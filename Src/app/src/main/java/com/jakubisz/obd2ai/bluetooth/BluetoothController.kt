package com.jakubisz.obd2ai.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.jakubisz.obd2ai.model.BluetoothDeviceDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothAdapter: BluetoothAdapter?
        get() = context.getSystemService(BluetoothManager::class.java)?.adapter

    val isBluetoothEnabled: Boolean
        get() = runCatching { bluetoothAdapter?.isEnabled == true }.getOrDefault(false)

    @SuppressLint("MissingPermission")
    fun pairedDevices(): List<BluetoothDeviceDTO> {
        if (!hasPermissions(context)) return emptyList()
        return try {
            bluetoothAdapter?.bondedDevices
                ?.map { BluetoothDeviceDTO(name = it.name ?: "Unknown device", address = it.address) }
                ?.sortedBy { it.name }
                ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun deviceName(address: String): String? = try {
        bluetoothAdapter?.getRemoteDevice(address)?.name
    } catch (e: Exception) {
        null
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(address: String): BluetoothSocket = withContext(Dispatchers.IO) {
        val adapter = bluetoothAdapter ?: throw IOException("Bluetooth is not available on this device")
        val device = adapter.getRemoteDevice(address)
        val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        try {
            adapter.cancelDiscovery()
            socket.connect()
            socket
        } catch (e: IOException) {
            runCatching { socket.close() }
            throw e
        }
    }

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        fun requiredPermissions(): Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        fun hasPermissions(context: Context): Boolean =
            requiredPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
    }
}
