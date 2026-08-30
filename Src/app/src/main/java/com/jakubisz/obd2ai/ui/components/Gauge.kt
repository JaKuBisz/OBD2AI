package com.jakubisz.obd2ai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jakubisz.obd2ai.ui.theme.Danger
import com.jakubisz.obd2ai.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val START_ANGLE = 135f
private const val SWEEP_ANGLE = 270f

/**
 * Automotive-style gauge drawn with Canvas: arc, ticks, needle and a digital readout.
 */
@Composable
fun Gauge(
    value: Float?,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    dangerAbove: Float? = null
) {
    val arcColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val diameter = min(size.width, size.height)
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcStroke = Stroke(width = radius * 0.09f, cap = StrokeCap.Round)
            val arcPadding = radius * 0.08f
            val arcSize = androidx.compose.ui.geometry.Size(
                (radius - arcPadding) * 2f, (radius - arcPadding) * 2f
            )
            val topLeft = Offset(center.x - radius + arcPadding, center.y - radius + arcPadding)

            drawArc(
                color = arcColor,
                startAngle = START_ANGLE,
                sweepAngle = SWEEP_ANGLE,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = arcStroke
            )

            val safeValue = value?.coerceIn(minValue, maxValue)
            if (safeValue != null) {
                val fraction = (safeValue - minValue) / (maxValue - minValue)
                val isDanger = dangerAbove != null && safeValue >= dangerAbove
                drawArc(
                    color = if (isDanger) Danger else activeColor,
                    startAngle = START_ANGLE,
                    sweepAngle = SWEEP_ANGLE * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = arcStroke
                )

                // Needle
                val needleAngle = Math.toRadians((START_ANGLE + SWEEP_ANGLE * fraction).toDouble())
                val needleLength = radius - arcPadding - radius * 0.12f
                val needleEnd = Offset(
                    (center.x + needleLength * cos(needleAngle)).toFloat(),
                    (center.y + needleLength * sin(needleAngle)).toFloat()
                )
                drawLine(
                    color = if (isDanger) Danger else Color.White,
                    start = center,
                    end = needleEnd,
                    strokeWidth = radius * 0.03f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color.White, radius = radius * 0.05f, center = center)
            }

            // Ticks
            val tickCount = 9
            for (i in 0..tickCount) {
                val angle = Math.toRadians((START_ANGLE + SWEEP_ANGLE * i / tickCount).toDouble())
                val outer = radius - arcPadding * 0.4f
                val inner = outer - radius * 0.06f
                drawLine(
                    color = arcColor,
                    start = Offset(
                        (center.x + inner * cos(angle)).toFloat(),
                        (center.y + inner * sin(angle)).toFloat()
                    ),
                    end = Offset(
                        (center.x + outer * cos(angle)).toFloat(),
                        (center.y + outer * sin(angle)).toFloat()
                    ),
                    strokeWidth = radius * 0.015f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)
        ) {
            Text(
                text = value?.let { if (it % 1f == 0f) it.toInt().toString() else "%.1f".format(it) } ?: "--",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = unit, color = TextSecondary, fontSize = 11.sp)
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
