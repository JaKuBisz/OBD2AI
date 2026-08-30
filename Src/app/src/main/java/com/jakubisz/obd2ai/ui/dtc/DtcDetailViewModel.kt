package com.jakubisz.obd2ai.ui.dtc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.data.ai.AiRepository
import com.jakubisz.obd2ai.model.DtpCodeDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DtcDetailUiState(
    val info: DtpCodeDTO? = null,
    val loadingAi: Boolean = false,
    val fromOfflineDb: Boolean = false,
    val aiAvailable: Boolean = false
)

@HiltViewModel
class DtcDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val code: String = checkNotNull(savedStateHandle["code"])

    private val _uiState = MutableStateFlow(DtcDetailUiState(aiAvailable = aiRepository.isAiAvailable))
    val uiState: StateFlow<DtcDetailUiState> = _uiState.asStateFlow()

    init {
        // Show offline info immediately, then enrich with AI when available.
        val offline = aiRepository.offlineLookup(code)
        _uiState.value = _uiState.value.copy(info = offline, fromOfflineDb = offline != null)
        if (aiRepository.isAiAvailable) {
            explainWithAi()
        }
    }

    fun explainWithAi() {
        if (_uiState.value.loadingAi) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingAi = true)
            val result = aiRepository.assess(code)
            _uiState.value = _uiState.value.copy(
                info = result,
                loadingAi = false,
                fromOfflineDb = false
            )
        }
    }
}
