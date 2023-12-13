package com.jakubisz.obd2ai

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest.permission
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jakubisz.obd2ai.ui.theme.OBD2AITheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.lifecycleScope
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.control.DTCNumberCommand
import com.github.eltonvs.obd.command.control.PendingTroubleCodesCommand
import com.github.eltonvs.obd.command.control.PermanentTroubleCodesCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.temperature.AirIntakeTemperatureCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
var obdDeviceConnection: ObdDeviceConnection? = null
var isConnected = false

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {
    var bluetoothAdapter: BluetoothAdapter? = null
    var obdSocket: BluetoothSocket? = null
    // UUID for SPP (Serial Port Profile)
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OBD2AITheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
        checkBluetoothPermissions()
    }

    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, permission.BLUETOOTH_ADMIN)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permissions are not granted. Request permissions.
            ActivityCompat.requestPermissions(this, arrayOf(
                permission.BLUETOOTH,
                permission.BLUETOOTH_ADMIN,
                permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_PERMISSIONS)
        } else {
            // Permissions already granted. Proceed with Bluetooth operations.
            setupBluetooth()

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<out String>,
                                   grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted. You can proceed with your Bluetooth operations.
                setupBluetooth()
            } else {
                // Permissions denied. Handle the feature's unavailability.
                showPermissionsDeniedDialog()
            }
        }
    }

    private fun showPermissionsDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Bluetooth permissions are required for this app to function. The app will close now.")
            .setPositiveButton("OK") { _, _ ->
                finish() // Close the app
            }
            .setCancelable(false)
            .show()
    }

    suspend fun connectToObdDevice(deviceAddress: String) : ObdDeviceConnection {
        val device: BluetoothDevice = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            ?: throw IOException("Device not found")


        obdSocket = device.createRfcommSocketToServiceRecord(sppUuid).apply {
            // Cancel discovery as it may slow down the connection
            bluetoothAdapter?.cancelDiscovery()

            // Connect to the remote device
            connect()
        }
        if (obdSocket == null) {
            throw IOException("OBD Socket is null")
        }

        // After connection, you can use the input and output streams to communicate
        val inputStream = obdSocket?.inputStream
        val outputStream = obdSocket?.outputStream

        if (inputStream == null || outputStream == null) {
            throw IOException("Socket streams are null")
        }

        // Send OBD commands and read responses
        // Create ObdDeviceConnection instance
        return ObdDeviceConnection(inputStream, outputStream)
    }

    private fun setupBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(this, enableBtIntent, REQUEST_CODE_PERMISSIONS, null)
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
    private fun GetResult(result : ObdDeviceConnection) {
        isConnected = true
        obdDeviceConnection = result
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var showResults by remember { mutableStateOf(false) }
    var communicationKey by remember { mutableStateOf(0) }

    Button(onClick = { communicationKey++  }) {
        Text("Start OBD Communication")
    }
    ObdCommunicator(key = communicationKey)

    if (showResults) {
        //ObdCommunicator()
    }
    //ObdCommunicator()
    //BluetoothComponent()
}

@Composable
fun ObdCommunicator (key: Int) {
    val obdDeviceConnection = obdDeviceConnection ?: return

    val responses = remember { mutableStateListOf<ObdResponse>() }
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf<String?>("") }

    // LaunchedEffect is used to perform side effects in Compose
    LaunchedEffect(key) {
        launch {

            try {
                // Execute blocking operation in a coroutine
                /*
                responses.add(obdDeviceConnection.run(SpeedCommand()))
                responses.add(obdDeviceConnection.run(VINCommand()))
                responses.add(obdDeviceConnection.run(RPMCommand()))
                responses.add(obdDeviceConnection.run(AirIntakeTemperatureCommand()))*/
                responses.add(obdDeviceConnection.run(TroubleCodesCommand()))
                responses.add(obdDeviceConnection.run(PendingTroubleCodesCommand()))
                responses.add(obdDeviceConnection.run(PermanentTroubleCodesCommand()))

            } catch (e: Exception) {
                errorMsg.value = e.message
                showErrorDialog.value = true
            }
        }
    }

    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            title = { Text("Error") },
            text = { Text(errorMsg.value ?: "") },
            confirmButton = {
                Button(onClick = { showErrorDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
    // UI is automatically recomposed when 'speed' changes
    //TextComponent("Data:")
    if (responses.isNotEmpty()) {
        Column {
            responses.forEach { response ->
                ResponseRow(response)
            }
        }
    }
}

@Composable
fun ResponseRow(response: ObdResponse) {
    Row {
        TextComponent(response.formattedValue)
        TextComponent(" | ")
        TextComponent(response.value)
        TextComponent(" | ")
        TextComponent(response.unit)
        TextComponent(" | ")
        TextComponent(response.rawResponse.toString())
    }
}

@Composable
fun ResponseRow(response: String) {
    Row {
        TextComponent("1$response")
        TextComponent("2$response")
        TextComponent("3$response")
        TextComponent("4$response")
    }
}

@Composable
fun TextComponent(result : String) {
    Text(text = result)
}
/*
@Composable
fun BluetoothComponent() {
    //Show list of bluetooth devices
    val bluetoothService = BluetoothService()
    val bluetoothDevices = bluetoothService.getConnectedDevices()
    bluetoothDevices?.forEach {
        Text(text = it.name)
    }

}
*/


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OBD2AITheme {
        Greeting("Android")
    }
}