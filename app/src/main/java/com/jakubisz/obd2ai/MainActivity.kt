package com.jakubisz.obd2ai

import android.os.Bundle
import android.app.AlertDialog
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
import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.control.PendingTroubleCodesCommand
import com.github.eltonvs.obd.command.control.PermanentTroubleCodesCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

var obdDeviceConnection: ObdDeviceConnection? = null
var isConnected = false

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {
    private lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var obdHelper: ObdHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initHelpers()
        val bluetoothTestViewModel = TestViewModel(bluetoothHelper, obdHelper, OpenAIService())
        setContent {
            OBD2AITheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    //Greeting("Android")
                    TestFancy(viewModel = bluetoothTestViewModel)
                }
            }
        }
        //bluetoothHelper.checkBluetoothPermissions()
    }

    private fun initHelpers() {
        bluetoothHelper = BluetoothHelper(this)
        obdHelper = ObdHelper(bluetoothHelper)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<out String>,
                                   grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (bluetoothHelper.resolvePermissionsResult(requestCode, grantResults)) {
            // Permissions granted. You can proceed with your Bluetooth operations.
            //setupObd()
        } else {
            // Permissions denied. Handle the feature's unavailability.
            //showPermissionsDeniedDialog()
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

    private fun setupObd() {
        bluetoothHelper.setupBluetooth()
        lifecycleScope.launch(Dispatchers.IO) {
            obdHelper.setupObd("00:1D:A5:68:98:8B")
            val result = obdHelper.getObdDeviceConnection()
                withContext(Dispatchers.Main) {
                // Update UI with the result
                GetResult(result)
            }
        }

        //connectToObdDevice("00:1D:A5:68:98:8B")
    }
    private fun GetResult(result : ObdDeviceConnection?) {
        isConnected = result != null
        obdDeviceConnection = result
    }
}

@SuppressLint("MissingPermission")
@Composable
fun TestFancy(viewModel: TestViewModel) {
    var triggerApiCall by remember { mutableStateOf(false) }
    var triggerDiagnosticCall by remember { mutableStateOf(false) }

    val onSuccess = { connection: Pair<InputStream, OutputStream> ->
        // Handle successful connection here
        // For example, you can update the UI to show a successful connection message
        // or start using the connection streams
    }

    // Define the onFailure callback
    val onFailure = { exception: IOException ->
        // Handle failure here
        // For example, you can show an error message to the user
    }
    val activity = LocalContext.current as Activity
    var displayText by remember { mutableStateOf("Test") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 18.sp
        )

        Button(
            onClick = { viewModel.requestBluetoothPermissions(activity) },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Ask permissions for BT")
        }
        Button(
            onClick = { viewModel.enableBluetooth(activity) },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Enable BT")
        }
        Button(
            onClick = { displayText = viewModel.getAvailableDevices().joinToString("\n") { "Name: ${it.name}, Address: ${it.address}" } },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("List devices")
        }
        Button(
            onClick = { viewModel.connectToDevice("00:1D:A5:68:98:8B", onSuccess, onFailure) },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Connect to OBD2")
        }
        Button(
            onClick = { triggerApiCall = !triggerApiCall },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Test API")
        }
        Button(
            onClick = { triggerDiagnosticCall = !triggerDiagnosticCall },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Start diagnostic (dummy)")
        }
        /*
        Button(onClick = { /* Start communication logic */ }) {
            Text("Start the communication with OBD2")
        }
        */

        LaunchedEffect(triggerApiCall) {
            displayText = viewModel.testApi()
        }
        LaunchedEffect(triggerDiagnosticCall) {
            displayText = viewModel.getDtpErrorExplanationExample()
        }

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