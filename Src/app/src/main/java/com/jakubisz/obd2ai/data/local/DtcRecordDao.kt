package com.jakubisz.obd2ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DtcRecordDao {
    @Insert
    suspend fun insertAll(records: List<DtcRecordEntity>)

    @Query("SELECT * FROM dtc_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<DtcRecordEntity>>

    @Query("DELETE FROM dtc_records")
    suspend fun deleteAll()
}
