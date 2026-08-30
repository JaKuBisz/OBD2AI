package com.jakubisz.obd2ai.ui.dtc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.data.ai.AiRepository
import com.jakubisz.obd2ai.data.local.DtcRecordDao
import com.jakubisz.obd2ai.data.local.DtcRecordEntity
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.data.obd.ObdRepository
import com.jakubisz.obd2ai.model.DtpCodeDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DtcScanUiState(
    val scanning: Boolean = false,
    val results: List<DtpCodeDTO> = emptyList(),
    val scannedOnce: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DtcScanViewModel @Inject constructor(
    private val obdRepository: ObdRepository,
    private val aiRepository: AiRepository,
    private val dtcRecordDao: DtcRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DtcScanUiState())
    val uiState: StateFlow<DtcScanUiState> = _uiState.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = obdRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    fun scan() {
        if (_uiState.value.scanning) return
        viewModelScope.launch {
            _uiState.value = DtcScanUiState(scanning = true, scannedOnce = true)
            try {
                val codes = obdRepository.readTroubleCodes()
                // Offline info renders instantly; AI explanations are fetched on the detail screen.
                val results = codes.map { code ->
                    aiRepository.offlineLookup(code) ?: aiRepository.assess(code)
                }
                persistScan(results)
                _uiState.value = DtcScanUiState(results = results, scannedOnce = true)
            } catch (e: Exception) {
                _uiState.value = DtcScanUiState(scannedOnce = true, error = e.message ?: "Scan failed")
            }
        }
    }

    private suspend fun persistScan(results: List<DtpCodeDTO>) {
        if (results.isEmpty()) return
        val now = System.currentTimeMillis()
        dtcRecordDao.insertAll(
            results.map {
                DtcRecordEntity(
                    code = it.errorCode,
                    severity = it.severity.ordinal,
                    title = it.title,
                    timestamp = now
                )
            }
        )
    }
}
