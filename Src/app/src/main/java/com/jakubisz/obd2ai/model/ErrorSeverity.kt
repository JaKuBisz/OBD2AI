package com.jakubisz.obd2ai.model

import android.graphics.Color
enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun fromInt(value: Int) = when (value) {
            0 -> LOW
            1 -> MEDIUM
            2 -> HIGH
            else -> throw IllegalArgumentException("Invalid severity level: $value")
        }

        fun getColor(severity: ErrorSeverity) = when (severity) {
            MEDIUM -> Color.rgb(255, 165, 0) // Orange color
            HIGH -> Color.RED
            else -> Color.GRAY
        }
    }
}