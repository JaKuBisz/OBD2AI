package com.jakubisz.obd2ai.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.ui.components.Gauge
import com.jakubisz.obd2ai.ui.components.ObdTopBar
import com.jakubisz.obd2ai.ui.theme.Ok
import com.jakubisz.obd2ai.ui.theme.Surface
import com.jakubisz.obd2ai.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    demo: Boolean,
    onBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val reading by viewModel.reading.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    LaunchedEffect(demo) { viewModel.start(demo) }
    DisposableEffect(Unit) { onDispose { viewModel.stop() } }

    Scaffold(topBar = { ObdTopBar("Live dashboard", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isLive = demo || connectionState is ConnectionState.Connected
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isLive) Ok.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (demo) "DEMO MODE" else if (isLive) "LIVE" else "NOT CONNECTED",
                    color = if (isLive) Ok else TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Gauge(
                    value = reading.rpm?.toFloat(),
                    minValue = 0f,
                    maxValue = 8000f,
                    label = "Engine",
                    unit = "RPM",
                    dangerAbove = 5500f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.4f)
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallGaugeCard(
                    modifier = Modifier.weight(1f),
                    value = reading.speedKmh?.toFloat(),
                    maxValue = 240f,
                    label = "Speed",
                    unit = "km/h"
                )
                SmallGaugeCard(
                    modifier = Modifier.weight(1f),
                    value = reading.coolantTempC?.toFloat(),
                    maxValue = 140f,
                    label = "Coolant",
                    unit = "°C",
                    dangerAbove = 105f
                )
                SmallGaugeCard(
                    modifier = Modifier.weight(1f),
                    value = reading.fuelLevelPercent,
                    maxValue = 100f,
                    label = "Fuel",
                    unit = "%"
                )
            }
        }
    }
}

@Composable
private fun SmallGaugeCard(
    value: Float?,
    maxValue: Float,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    dangerAbove: Float? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Gauge(
            value = value,
            minValue = 0f,
            maxValue = maxValue,
            label = label,
            unit = unit,
            dangerAbove = dangerAbove,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
        )
    }
}
