package com.jakubisz.obd2ai

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

//BTManager overview
//bool CheckIfAll required permissions are granted
//void RequestPermissions
//void EnableBluetoothIfPermitted
//... GetAvailableDevices
//void ConnectToDevice - return input and output streams
//void DisconnectFromDev
//

class BluetoothManager(private val context: Context) {
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101

        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun checkBluetoothPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Throws(SecurityException::class)
    fun throwIfPermissionsNotGranted() {
        if (!checkBluetoothPermissions()) {
            throw SecurityException("Required Bluetooth permissions are not granted")
        }
    }

    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    @Throws(SecurityException::class)
    fun enableBluetooth() {
        throwIfPermissionsNotGranted()

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        }
    }

    @Throws(SecurityException::class)
    fun getAvailableDevices(): Set<BluetoothDevice>? {
        throwIfPermissionsNotGranted()

        return bluetoothAdapter?.bondedDevices
    }

    @Throws(IOException::class, SecurityException::class)
    fun connectToDevice(deviceAddress: String): Pair<InputStream?, OutputStream?> {
        // Check for permissions before proceeding
        throwIfPermissionsNotGranted()

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            ?: throw IOException("Device not found")

        bluetoothSocket = device.createRfcommSocketToServiceRecord(sppUuid).apply {
            try {
                // Cancel discovery as it may slow down the connection
                bluetoothAdapter?.cancelDiscovery()

                // Connect to the remote device
                connect()
            } catch (e: SecurityException) {
                throw IOException("Failed to connect to device due to security restrictions", e)
            }
        }

        return Pair(bluetoothSocket?.inputStream, bluetoothSocket?.outputStream)
    }

    fun disconnectFromDevice() {
        bluetoothSocket?.close()
        bluetoothSocket = null
    }

    private fun setupBluetooth() {
        val bluetoothManager = getSystemService(context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(
                this,
                enableBtIntent,
                MainActivity.REQUEST_CODE_PERMISSIONS,
                null
            )
        }
        val devices = bluetoothAdapter?.bondedDevices
        devices?.forEach {
            println(it.name)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val result = connectToObdDevice("00:1D:A5:68:98:8B")
            withContext(Dispatchers.Main) {
                // Update UI with the result
                GetResult(result)
            }
        }

        //connectToObdDevice("00:1D:A5:68:98:8B")
    }
}
