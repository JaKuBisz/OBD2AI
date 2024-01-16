package com.jakubisz.obd2ai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jakubisz.obd2ai.helpers.BluetoothHelper
import com.jakubisz.obd2ai.helpers.ObdHelper
import com.jakubisz.obd2ai.helpers.OpenAIService

class ConnectorViewModelFactory(
    private val bluetoothHelper: BluetoothHelper,
    private val obdHelper: ObdHelper,
    private val openAI: OpenAIService
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConnectorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConnectorViewModel(bluetoothHelper, obdHelper, openAI) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }