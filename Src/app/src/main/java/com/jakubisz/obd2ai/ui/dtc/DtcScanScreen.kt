package com.jakubisz.obd2ai.ui.dtc

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.ui.components.HintText
import com.jakubisz.obd2ai.ui.components.ObdTopBar
import com.jakubisz.obd2ai.ui.components.SeverityChip
import com.jakubisz.obd2ai.ui.theme.Surface
import com.jakubisz.obd2ai.ui.theme.TextSecondary

@Composable
fun DtcScanScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onConnect: () -> Unit,
    viewModel: DtcScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val connected = connectionState is ConnectionState.Connected

    Scaffold(topBar = { ObdTopBar("Diagnostics", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!connected) {
                HintText("Connect to your vehicle before scanning for trouble codes.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = onConnect, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Connect device")
                }
            } else {
                Button(
                    onClick = { viewModel.scan() },
                    enabled = !uiState.scanning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(if (uiState.scanning) "Scanning…" else "Scan for trouble codes")
                }

                when {
                    uiState.scanning -> Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                        Text("Reading codes from ECU…")
                    }
                    uiState.error != null -> HintText("Scan failed: ${uiState.error}")
                    uiState.scannedOnce && uiState.results.isEmpty() ->
                        HintText("No trouble codes found. Your car is happy ✅")
                }

                LazyColumn {
                    items(uiState.results) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onOpenDetail(item.errorCode) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.errorCode, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                SeverityChip(item.severity)
                            }
                        }
                    }
                }
            }
        }
    }
}
