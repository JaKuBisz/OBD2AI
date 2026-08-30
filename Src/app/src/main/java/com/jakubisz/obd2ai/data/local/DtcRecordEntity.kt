package com.jakubisz.obd2ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dtc_records")
data class DtcRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val severity: Int,
    val title: String,
    val timestamp: Long
)
