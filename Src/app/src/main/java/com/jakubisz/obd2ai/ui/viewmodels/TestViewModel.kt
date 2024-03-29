package com.jakubisz.obd2ai.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.helpers.BluetoothHelper
import com.jakubisz.obd2ai.helpers.ObdHelper
import com.jakubisz.obd2ai.helpers.OpenAIService
import com.jakubisz.obd2ai.model.BluetoothDeviceDTO
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class TestViewModel(private val bluetoothHelper: BluetoothHelper, private val obdHelper: ObdHelper, private val openAI: OpenAIService) : ViewModel() {
    // Function to request Bluetooth permissions
    fun requestBluetoothPermissions(activity: Activity) {
        bluetoothHelper.requestPermissions(activity)
    }

    // Function to enable Bluetooth
    fun enableBluetooth(activity: Activity) {
        viewModelScope.launch {
            try {
                bluetoothHelper.enableBluetooth()
            } catch (e: SecurityException) {
                // Handle if permissions are not granted
                bluetoothHelper.requestPermissions(activity)
            }
        }
    }

    // Function to get available devices
    fun getAvailableDevices(): List<BluetoothDeviceDTO> {
        return bluetoothHelper.getAvailableDevices()
    }

    // Function to connect to a device
    fun connectToDevice(deviceAddress: String, onSuccess: (Pair<InputStream, OutputStream>) -> Unit, onFailure: (IOException) -> Unit) {
        viewModelScope.launch {
            try {
                val connection = bluetoothHelper.connectToDevice(deviceAddress)
                onSuccess(connection)
            } catch (e: IOException) {
                onFailure(e)
            }
        }
    }

    suspend fun getDtpErrorExplanation() : String
    {
        //Example of function
        obdHelper.setupObd("00:1D:A5:68:98:8B")
        var connection = obdHelper.getObdDeviceConnection()
        var dtp = obdHelper.getDtpCodes()
        var pendDtp = obdHelper.getPendingDtpCodes()
        var permDtp = obdHelper.getPermanentDtpCodes()
        var query = "Assess these DTP codes for Renault Clio 2001: Classic error codes: {$dtp}, Pending error codes: {$pendDtp}, Permanent(deleted) error codes: {$permDtp}"
        var response = openAI.getResponse(query)
        return response
    }
    suspend fun getDtpErrorExplanationExample() : String
    {
        //Example of function
        //obdHelper.setupObd("00:1D:A5:68:98:8B")
        //var connection = obdHelper.getObdDeviceConnection()
        var dtp = "P504, P500"
        var pendDtp = "P0532, P0331"
        var permDtp = "P0136, P3404"
        var query = "Assess these DTP codes for Renault Clio 2001: Classic error codes: {$dtp}, Pending error codes: {$pendDtp}, Permanent(deleted) error codes: {$permDtp}"
        var response = openAI.getResponse(query)
        return response
    }

    suspend fun testApi(): String {
        return openAI.getResponse("This is test say something smart.")
    }

    // Function to disconnect from a device
    fun disconnectFromDevice() {
        bluetoothHelper.disconnectFromDevice()
    }
    // Additional functions as needed
}
