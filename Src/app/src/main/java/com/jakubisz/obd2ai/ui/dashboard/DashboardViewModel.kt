package com.jakubisz.obd2ai.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.data.local.TripSessionDao
import com.jakubisz.obd2ai.data.local.TripSessionEntity
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.data.obd.ObdRepository
import com.jakubisz.obd2ai.model.LiveReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sin

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obdRepository: ObdRepository,
    private val tripSessionDao: TripSessionDao
) : ViewModel() {

    private val _reading = MutableStateFlow(LiveReading.EMPTY)
    val reading: StateFlow<LiveReading> = _reading.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = obdRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    private var collectJob: Job? = null
    private var demoMode = false

    // Session stats for history
    private var startedAt = 0L
    private var sampleCount = 0
    private var maxRpm = 0
    private var maxSpeed = 0
    private var coolantSum = 0L

    fun start(demo: Boolean) {
        if (collectJob != null) return
        demoMode = demo
        startedAt = System.currentTimeMillis()
        collectJob = viewModelScope.launch {
            val source = if (demo) simulateLiveData() else obdRepository.observeLiveData()
            source.collect { value ->
                _reading.value = value
                record(value)
            }
        }
    }

    fun stop() {
        collectJob?.cancel()
        collectJob = null
        saveSession()
    }

    private fun record(value: LiveReading) {
        sampleCount++
        value.rpm?.let { if (it > maxRpm) maxRpm = it }
        value.speedKmh?.let { if (it > maxSpeed) maxSpeed = it }
        value.coolantTempC?.let { coolantSum += it }
    }

    private fun saveSession() {
        if (sampleCount == 0) return
        val session = TripSessionEntity(
            startedAt = startedAt,
            endedAt = System.currentTimeMillis(),
            sampleCount = sampleCount,
            maxRpm = maxRpm,
            maxSpeedKmh = maxSpeed,
            avgCoolantTempC = if (sampleCount > 0) (coolantSum / sampleCount).toInt() else 0,
            demo = demoMode
        )
        viewModelScope.launch { tripSessionDao.insert(session) }
        sampleCount = 0
        maxRpm = 0
        maxSpeed = 0
        coolantSum = 0
    }

    override fun onCleared() {
        collectJob?.cancel()
        saveSession()
        super.onCleared()
    }

    /** Simulated sensor data so the dashboard can be demoed without a car. */
    private fun simulateLiveData() = flow {
        var t = 0.0
        while (currentCoroutineContext().isActive) {
            emit(
                LiveReading(
                    rpm = (2800 + 2200 * sin(t)).toInt().coerceIn(800, 6500),
                    speedKmh = (70 + 55 * sin(t * 0.7)).toInt().coerceIn(0, 200),
                    coolantTempC = (88 + 6 * sin(t * 0.2)).toInt(),
                    fuelLevelPercent = (62f - (t / 10f)).toFloat().coerceIn(0f, 100f),
                    timestamp = System.currentTimeMillis()
                )
            )
            t += 0.15
            delay(300)
        }
    }
}
