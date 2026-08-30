package com.jakubisz.obd2ai.ui.connect

import android.bluetooth.BluetoothAdapter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jakubisz.obd2ai.bluetooth.BluetoothController
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.ui.components.HintText
import com.jakubisz.obd2ai.ui.components.ObdTopBar
import com.jakubisz.obd2ai.ui.theme.Surface
import com.jakubisz.obd2ai.ui.theme.TextSecondary

@Composable
fun ConnectScreen(
    onBack: () -> Unit,
    onDemo: () -> Unit,
    onConnected: () -> Unit,
    viewModel: ConnectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()

    var permissionsGranted by remember {
        mutableStateOf(BluetoothController.hasPermissions(context))
    }
    var bluetoothEnabled by remember { mutableStateOf(viewModel.isBluetoothEnabled) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> permissionsGranted = result.values.all { it } }

    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { bluetoothEnabled = viewModel.isBluetoothEnabled }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionLauncher.launch(BluetoothController.requiredPermissions())
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) onConnected()
    }

    Scaffold(topBar = { ObdTopBar("Connect device", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                !permissionsGranted -> {
                    HintText("OBD2AI needs Bluetooth and location permissions to talk to your ELM327 adapter.")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(BluetoothController.requiredPermissions()) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) { Text("Grant permissions") }
                }
                !bluetoothEnabled -> {
                    HintText("Bluetooth is turned off.")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            runCatching {
                                enableBtLauncher.launch(android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) { Text("Turn on Bluetooth") }
                }
                else -> {
                    when (val state = connectionState) {
                        ConnectionState.Connecting -> {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                                Text("Connecting…")
                            }
                        }
                        is ConnectionState.Error -> {
                            HintText("Could not connect: ${state.message}")
                        }
                        else -> Unit
                    }

                    val devices = remember(permissionsGranted, bluetoothEnabled) {
                        viewModel.pairedDevices()
                    }

                    if (devices.isEmpty()) {
                        HintText("No paired devices found. Pair your ELM327 adapter in Android Bluetooth settings first.")
                    } else {
                        HintText("Paired devices")
                        LazyColumn {
                            items(devices) { device ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .clickable { viewModel.connect(device.address) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Bluetooth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column(Modifier.padding(start = 12.dp)) {
                                            Text(device.name, style = MaterialTheme.typography.titleLarge)
                                            Text(
                                                device.address,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = onDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("No adapter? Try demo mode") }
        }
    }
}
