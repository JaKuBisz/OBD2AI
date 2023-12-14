package com.jakubisz.obd2ai

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothTestViewModel(private val bluetoothHelper: BluetoothHelper) : ViewModel() {
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
    fun getAvailableDevices(): Set<BluetoothDevice> {
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

    // Function to disconnect from a device
    fun disconnectFromDevice() {
        bluetoothHelper.disconnectFromDevice()
    }
    // Additional functions as needed
}
