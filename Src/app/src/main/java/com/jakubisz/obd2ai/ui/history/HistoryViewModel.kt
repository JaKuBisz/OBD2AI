package com.jakubisz.obd2ai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.data.local.DtcRecordDao
import com.jakubisz.obd2ai.data.local.DtcRecordEntity
import com.jakubisz.obd2ai.data.local.TripSessionDao
import com.jakubisz.obd2ai.data.local.TripSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dtcRecordDao: DtcRecordDao,
    private val tripSessionDao: TripSessionDao
) : ViewModel() {

    val dtcRecords: StateFlow<List<DtcRecordEntity>> = dtcRecordDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tripSessions: StateFlow<List<TripSessionEntity>> = tripSessionDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch {
            dtcRecordDao.deleteAll()
            tripSessionDao.deleteAll()
        }
    }
}
