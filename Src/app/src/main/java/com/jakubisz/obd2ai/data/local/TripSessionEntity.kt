package com.jakubisz.obd2ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_sessions")
data class TripSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startedAt: Long,
    val endedAt: Long,
    val sampleCount: Int,
    val maxRpm: Int,
    val maxSpeedKmh: Int,
    val avgCoolantTempC: Int,
    val demo: Boolean
)
