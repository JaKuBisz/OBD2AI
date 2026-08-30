package com.jakubisz.obd2ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DtcRecordEntity::class, TripSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dtcRecordDao(): DtcRecordDao
    abstract fun tripSessionDao(): TripSessionDao
}
