package com.jakubisz.obd2ai.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jakubisz.obd2ai.model.ErrorSeverity
import com.jakubisz.obd2ai.ui.components.HintText
import com.jakubisz.obd2ai.ui.components.ObdTopBar
import com.jakubisz.obd2ai.ui.components.SectionHeader
import com.jakubisz.obd2ai.ui.components.SeverityChip
import com.jakubisz.obd2ai.ui.theme.Surface
import com.jakubisz.obd2ai.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val dtcRecords by viewModel.dtcRecords.collectAsState()
    val tripSessions by viewModel.tripSessions.collectAsState()

    Scaffold(
        topBar = { ObdTopBar("History", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Scan history", Modifier.weight(1f))
                    if (dtcRecords.isNotEmpty() || tripSessions.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAll() }) { Text("Clear all") }
                    }
                }
            }

            if (dtcRecords.isEmpty()) {
                item { HintText("No scans yet.") }
            }
            items(dtcRecords.size) { index ->
                val record = dtcRecords[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(record.code, style = MaterialTheme.typography.titleLarge)
                            Text(record.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatTimestamp(record.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        SeverityChip(severityFromOrdinal(record.severity))
                    }
                }
            }

            item { SectionHeader("Trip history") }

            if (tripSessions.isEmpty()) {
                item { HintText("No recorded sessions yet. Open the live dashboard while driving.") }
            }
            items(tripSessions.size) { index ->
                val session = tripSessions[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            formatTimestamp(session.startedAt) + if (session.demo) " (demo)" else "",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Duration ${formatDuration(session.startedAt, session.endedAt)}  •  " +
                                "max ${session.maxRpm} rpm  •  max ${session.maxSpeedKmh} km/h  •  " +
                                "avg coolant ${session.avgCoolantTempC} °C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun severityFromOrdinal(ordinal: Int): ErrorSeverity =
    ErrorSeverity.entries.getOrElse(ordinal) { ErrorSeverity.LOW }

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatDuration(start: Long, end: Long): String {
    val seconds = ((end - start) / 1000).coerceAtLeast(0)
    return if (seconds >= 60) "${seconds / 60} min" else "$seconds s"
}
