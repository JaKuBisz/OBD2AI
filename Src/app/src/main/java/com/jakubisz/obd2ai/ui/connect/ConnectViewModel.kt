package com.jakubisz.obd2ai.ui.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.bluetooth.BluetoothController
import com.jakubisz.obd2ai.data.obd.ConnectionState
import com.jakubisz.obd2ai.data.obd.ObdRepository
import com.jakubisz.obd2ai.model.BluetoothDeviceDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val obdRepository: ObdRepository,
    private val bluetooth: BluetoothController
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = obdRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    val isBluetoothEnabled: Boolean
        get() = bluetooth.isBluetoothEnabled

    fun pairedDevices(): List<BluetoothDeviceDTO> = bluetooth.pairedDevices()

    fun connect(address: String) {
        viewModelScope.launch { obdRepository.connect(address) }
    }

    fun disconnect() = obdRepository.disconnect()
}
