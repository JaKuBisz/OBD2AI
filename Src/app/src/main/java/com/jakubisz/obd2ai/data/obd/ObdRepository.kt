package com.jakubisz.obd2ai.data.obd

import android.bluetooth.BluetoothSocket
import com.github.eltonvs.obd.command.control.PendingTroubleCodesCommand
import com.github.eltonvs.obd.command.control.PermanentTroubleCodesCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.fuel.FuelLevelCommand
import com.github.eltonvs.obd.command.temperature.EngineCoolantTemperatureCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import com.jakubisz.obd2ai.bluetooth.BluetoothController
import com.jakubisz.obd2ai.model.LiveReading
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val deviceName: String, val vin: String?) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

@Singleton
class ObdRepository @Inject constructor(
    private val bluetooth: BluetoothController
) {
    private var socket: BluetoothSocket? = null
    private var connection: ObdDeviceConnection? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    val isConnected: Boolean
        get() = _connectionState.value is ConnectionState.Connected

    suspend fun connect(deviceAddress: String) {
        if (_connectionState.value is ConnectionState.Connected) return
        _connectionState.value = ConnectionState.Connecting
        runCatching {
            val newSocket = bluetooth.connect(deviceAddress)
            val newConnection = ObdDeviceConnection(newSocket.inputStream, newSocket.outputStream)
            newSocket to newConnection
        }.onSuccess { (newSocket, newConnection) ->
            socket = newSocket
            connection = newConnection
            val vin = runCatching { newConnection.run(VINCommand()).formattedValue }.getOrNull()
                ?.takeIf { it.isNotBlank() }
            _connectionState.value = ConnectionState.Connected(
                deviceName = bluetooth.deviceName(deviceAddress) ?: deviceAddress,
                vin = vin
            )
        }.onFailure { error ->
            _connectionState.value = ConnectionState.Error(error.message ?: "Connection failed")
        }
    }

    fun disconnect() {
        runCatching { socket?.close() }
        socket = null
        connection = null
        _connectionState.value = ConnectionState.Disconnected
    }

    suspend fun readTroubleCodes(): List<String> {
        val conn = connection ?: throw IOException("Not connected to an OBD2 device")
        val raw = buildList {
            add(conn.run(TroubleCodesCommand()).formattedValue)
            add(conn.run(PendingTroubleCodesCommand()).formattedValue)
            add(conn.run(PermanentTroubleCodesCommand()).formattedValue)
        }
        return raw.flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals("OK", ignoreCase = true) && !it.equals("NO DATA", ignoreCase = true) }
            .distinct()
    }

    fun observeLiveData(intervalMs: Long = 400): Flow<LiveReading> = flow {
        while (currentCoroutineContext().isActive) {
            emit(readSnapshot())
            delay(intervalMs)
        }
    }

    private suspend fun readSnapshot(): LiveReading {
        val conn = connection ?: return LiveReading.EMPTY
        return LiveReading(
            rpm = runCatching { conn.run(RPMCommand()).value.toFloatOrNull()?.toInt() }.getOrNull(),
            speedKmh = runCatching { conn.run(SpeedCommand()).value.toFloatOrNull()?.toInt() }.getOrNull(),
            coolantTempC = runCatching { conn.run(EngineCoolantTemperatureCommand()).value.toFloatOrNull()?.toInt() }.getOrNull(),
            fuelLevelPercent = runCatching { conn.run(FuelLevelCommand()).value.toFloatOrNull() }.getOrNull()
        )
    }
}
