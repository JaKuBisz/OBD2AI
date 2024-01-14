package com.jakubisz.obd2ai

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

class BluetoothHelper(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter?
    private var bluetoothSocket: BluetoothSocket? = null
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

    private val _isBluetoothPermissionGranted = MutableLiveData<Boolean>()
    val isBluetoothPermissionGranted: LiveData<Boolean> = _isBluetoothPermissionGranted

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }
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

    fun setupBluetooth() {
        if(checkBluetoothPermissions()) {
            enableBluetooth()
        } else {
            //TODO:Pottencional issue when user denies permissions
            requestPermissions(context as Activity)
            enableBluetooth()
        }
        //val devices = getAvailableDevices()
        //connectToDevice("00:1D:A5:68:98:8B")
    }

    @Throws(SecurityException::class)
    fun enableBluetooth() {
        throwIfPermissionsNotGranted()

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        }
    }

    @Throws(SecurityException::class, IOException::class)
    fun getAvailableDevices(): List<BluetoothDeviceDTO> {
        throwIfPermissionsNotGranted()

        val bluetoothAdapter = bluetoothAdapter ?: throw IOException("Bluetooth adapter is null")

        val deviceDTOs = bluetoothAdapter.bondedDevices.map { convertToDeviceDTO(it) }
        return deviceDTOs
    }

    @Throws(IOException::class, SecurityException::class)
    suspend fun connectToDevice(deviceAddress: String): Pair<InputStream, OutputStream> = withContext(Dispatchers.IO) {
        // Check for permissions before proceeding
        throwIfPermissionsNotGranted()

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            ?: throw IOException("Device not found")

        bluetoothSocket = device.createRfcommSocketToServiceRecord(sppUuid).apply {
            try {
                // Cancel discovery as it may slow down the connection
                bluetoothAdapter.cancelDiscovery()

                // Connect to the remote device
                connect()
            } catch (e: SecurityException) {
                throw IOException("Failed to connect to device due to security restrictions", e)
            } catch (e: IOException) {
                throw IOException("Failed to connect to device", e)
            }
        }

        // Verify that the socket streams are not null
        val bluetoothSocket = bluetoothSocket ?: throw IOException("Bluetooth socket is null")

        Pair(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
    }

    fun disconnectFromDevice() {
        bluetoothSocket?.close()
        bluetoothSocket = null
    }

    // Method to handle permission results
    //Returns true if the result was granted, false otherwise
    fun resolvePermissionsResult(requestCode: Int, grantResults: IntArray): Boolean {
        var allPermissionsGranted = true

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    }
                }
            } else {
                allPermissionsGranted = false
            }
        }

        _isBluetoothPermissionGranted.value = allPermissionsGranted
        return allPermissionsGranted
    }

    @SuppressLint("MissingPermission")
    fun convertToDeviceDTO(bluetoothDevice: BluetoothDevice): BluetoothDeviceDTO {
        return BluetoothDeviceDTO(
            name = bluetoothDevice.name,
            address = bluetoothDevice.address
            // Map other fields if needed
        )
    }


    /*
    suspend fun runAutomatedBluetoothSetup(): Pair<InputStream, OutputStream> = withContext(Dispatchers.IO) {
        if(checkBluetoothPermissions()) {
            enableBluetooth()
        } else {
            //TODO:Pottencional issue when user denies permissions
            requestPermissions(context as Activity)

        }

        val devices = getAvailableDevices()

        connectToDevice("00:1D:A5:68:98:8B")
        //connectToObdDevice("00:1D:A5:68:98:8B")
    }*/
}
