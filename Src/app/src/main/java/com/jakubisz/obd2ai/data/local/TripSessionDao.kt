package com.jakubisz.obd2ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripSessionDao {
    @Insert
    suspend fun insert(session: TripSessionEntity)

    @Query("SELECT * FROM trip_sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TripSessionEntity>>

    @Query("DELETE FROM trip_sessions")
    suspend fun deleteAll()
}
