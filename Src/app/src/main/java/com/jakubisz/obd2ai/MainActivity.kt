package com.jakubisz.obd2ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jakubisz.obd2ai.ui.AppNavHost
import com.jakubisz.obd2ai.ui.theme.OBD2AITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OBD2AITheme {
                AppNavHost()
            }
        }
    }
}
