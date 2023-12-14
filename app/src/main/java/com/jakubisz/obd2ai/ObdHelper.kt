package com.jakubisz.obd2ai

import com.github.eltonvs.obd.connection.ObdDeviceConnection

class ObdHelper(private val bluetoothHelper: BluetoothHelper) {
    private var obdSocket: ObdDeviceConnection? = null

    suspend fun setupObd(deviceAddress: String) {
        val (inputStream, outputStream) = bluetoothHelper.connectToDevice(deviceAddress)
        obdSocket = ObdDeviceConnection(inputStream, outputStream)
    }

    fun getObdDeviceConnection(): ObdDeviceConnection? {
        return obdSocket
    }

    fun disconnectFromObdDevice() {
        bluetoothHelper.disconnectFromDevice()
    }



}
