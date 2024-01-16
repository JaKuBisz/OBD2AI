package com.jakubisz.obd2ai.helpers

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

    suspend fun getDtpCodes() : List<String>
    {
        val obdDeviceConnection = obdConnection ?: throw IOException("ObdDeviceConnection is null")
        val result = obdDeviceConnection.run(TroubleCodesCommand()).formattedValue
        return splitErrors(result)
    }
    suspend fun getPendingDtpCodes() : List<String>
    {
        val obdDeviceConnection = obdConnection ?: throw IOException("ObdDeviceConnection is null")
        val result =  obdDeviceConnection.run(PendingTroubleCodesCommand()).formattedValue
        return splitErrors(result)
    }
    suspend fun getPermanentDtpCodes() : List<String>
    {
        val obdDeviceConnection = obdConnection ?: throw IOException("ObdDeviceConnection is null")
        val result =  obdDeviceConnection.run(PermanentTroubleCodesCommand()).formattedValue
        return splitErrors(result)
    }
    suspend fun getAllDtpCodes() : List<String>
    {
        val responses = mutableListOf<String>()
        responses.addAll(getDtpCodes())
        responses.addAll(getPendingDtpCodes())
        responses.addAll(getPermanentDtpCodes())

        return responses
    }

    fun disconnectFromObdDevice() {
        bluetoothHelper.disconnectFromDevice()
    }

    fun splitErrors(errors: String) : List<String>
    {
        return errors.split(",").map { it.trim() }
    }


}
