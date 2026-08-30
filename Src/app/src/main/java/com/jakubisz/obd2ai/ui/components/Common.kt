package com.jakubisz.obd2ai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jakubisz.obd2ai.model.ErrorSeverity
import com.jakubisz.obd2ai.ui.theme.Background
import com.jakubisz.obd2ai.ui.theme.Danger
import com.jakubisz.obd2ai.ui.theme.Ok
import com.jakubisz.obd2ai.ui.theme.TextSecondary
import com.jakubisz.obd2ai.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObdTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background,
            titleContentColor = Color.White
        )
    )
}

fun severityColor(severity: ErrorSeverity): Color = when (severity) {
    ErrorSeverity.LOW -> Ok
    ErrorSeverity.MEDIUM -> Warning
    ErrorSeverity.HIGH -> Danger
}

@Composable
fun SeverityChip(severity: ErrorSeverity, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = severityColor(severity).copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = severity.name,
                color = severityColor(severity),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun HintText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
