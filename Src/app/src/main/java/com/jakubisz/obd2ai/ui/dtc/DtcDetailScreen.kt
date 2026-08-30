package com.jakubisz.obd2ai.ui.dtc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.jakubisz.obd2ai.ui.components.ObdTopBar
import com.jakubisz.obd2ai.ui.components.SeverityChip
import com.jakubisz.obd2ai.ui.theme.Surface
import com.jakubisz.obd2ai.ui.theme.TextSecondary

@Composable
fun DtcDetailScreen(
    onBack: () -> Unit,
    viewModel: DtcDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { ObdTopBar(uiState.info?.errorCode ?: "Error detail", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val info = uiState.info
            if (info == null) {
                Text("No information for this code.", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(info.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                SeverityChip(info.severity)
            }

            if (uiState.fromOfflineDb) {
                Text(
                    "From offline database",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (uiState.loadingAi) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                    Text("Asking AI for a deeper analysis…", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (uiState.aiAvailable && uiState.fromOfflineDb) {
                Button(
                    onClick = { viewModel.explainWithAi() },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Get AI analysis")
                }
            }

            DetailCard("What it means", info.detail)
            DetailCard("Implications", info.implications)

            if (info.suggestedActions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Suggested actions", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        info.suggestedActions.forEach { action ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                                )
                                Text(action, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, body: String) {
    if (body.isBlank()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
