package com.jakubisz.obd2ai.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.ui.theme.Ok
import com.jakubisz.obd2ai.ui.theme.Surface
import com.jakubisz.obd2ai.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onConnect: () -> Unit,
    onDashboard: () -> Unit,
    onScan: () -> Unit,
    onHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text("OBD2AI", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Intelligent car diagnostics",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(24.dp))
            ConnectionStatusCard(connectionState)
            Spacer(Modifier.height(24.dp))

            MenuCard("Connect device", "Pair with your ELM327 adapter", Icons.Filled.Bluetooth, onConnect)
            MenuCard("Live dashboard", "Real-time RPM, speed and temperatures", Icons.Filled.Speed, onDashboard)
            MenuCard("Scan diagnostics", "Read trouble codes with AI analysis", Icons.Filled.WarningAmber, onScan)
            MenuCard("History", "Past scans and trip sessions", Icons.Filled.History, onHistory)
        }
    }
}

@Composable
private fun ConnectionStatusCard(state: ConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                tint = if (state is ConnectionState.Connected) Ok else TextSecondary,
                modifier = Modifier.size(40.dp)
            )
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    text = when (state) {
                        is ConnectionState.Connected -> "Connected to ${state.deviceName}"
                        ConnectionState.Connecting -> "Connecting…"
                        ConnectionState.Disconnected -> "No vehicle connected"
                        is ConnectionState.Error -> "Connection failed"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                if (state is ConnectionState.Connected && state.vin != null) {
                    Text("VIN: ${state.vin}", style = MaterialTheme.typography.bodyMedium)
                }
                if (state is ConnectionState.Error) {
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun MenuCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
