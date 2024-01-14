package com.jakubisz.obd2ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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