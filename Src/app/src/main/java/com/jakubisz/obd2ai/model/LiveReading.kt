package com.jakubisz.obd2ai.model

/** A single snapshot of live vehicle sensor data. Null = PID not supported/readable. */
data class LiveReading(
    val rpm: Int? = null,
    val speedKmh: Int? = null,
    val coolantTempC: Int? = null,
    val fuelLevelPercent: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = LiveReading()
    }
}
