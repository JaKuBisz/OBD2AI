package com.jakubisz.obd2ai

import com.github.eltonvs.obd.command.control.PendingTroubleCodesCommand
import com.github.eltonvs.obd.command.control.PermanentTroubleCodesCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import java.io.IOException

class ObdHelper(private val bluetoothHelper: BluetoothHelper) {
    private var obdConnection: ObdDeviceConnection? = null

    suspend fun setupObd(deviceAddress: String) {
        val (inputStream, outputStream) = bluetoothHelper.connectToDevice(deviceAddress)
        obdConnection = ObdDeviceConnection(inputStream, outputStream)
    }

    fun getObdDeviceConnection(): ObdDeviceConnection? {
        return obdConnection
    }

    suspend fun getDtpCodes() : String
    {
        var obdDeviceConnection = obdConnection ?: throw IOException("ObdDeviceConnection is null")
        return obdDeviceConnection.run(TroubleCodesCommand()).formattedValue
    }
    suspend fun getPendingDtpCodes() : String
    {
        var obdDeviceConnection = obdConnection ?: throw IOException("ObdDeviceConnection is null")
        return obdDeviceConnection.run(PendingTroubleCodesCommand()).formattedValue
    }
    suspend fun getPermanentDtpCodes() : String
    {
        var obdDeviceConnection = obdConnection ?: throw IOException("ObdDeviceConnection is null")
        return obdDeviceConnection.run(PermanentTroubleCodesCommand()).formattedValue
    }
    suspend fun getAllDtpCodes() : String
    {
        var responses = mutableListOf<String>()
        responses.add(getDtpCodes())
        responses.add(getPendingDtpCodes())
        responses.add(getPermanentDtpCodes())

        return responses.joinToString(", ")
    }

    fun disconnectFromObdDevice() {
        bluetoothHelper.disconnectFromDevice()
    }



}
