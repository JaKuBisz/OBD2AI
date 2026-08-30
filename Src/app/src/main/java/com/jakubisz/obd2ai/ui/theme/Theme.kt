package com.jakubisz.obd2ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Automotive dark theme
val Background = Color(0xFF0B1220)
val Surface = Color(0xFF151F30)
val SurfaceVariant = Color(0xFF1E2A3D)
val Accent = Color(0xFF4DD0E1)
val AccentSecondary = Color(0xFFFFB74D)
val Danger = Color(0xFFEF5350)
val Warning = Color(0xFFFFB74D)
val Ok = Color(0xFF66BB6A)
val TextPrimary = Color(0xFFECEFF1)
val TextSecondary = Color(0xFF90A4AE)

private val ObdColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF00363D),
    secondary = AccentSecondary,
    onSecondary = Color(0xFF4A2800),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    onError = Color.White
)

private val ObdTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextSecondary
    )
)

@Composable
fun OBD2AITheme(content: @Composable () -> Unit) {
    // The app is a dashboard-style experience: always dark.
    MaterialTheme(
        colorScheme = ObdColorScheme,
        typography = ObdTypography,
        content = content
    )
}
