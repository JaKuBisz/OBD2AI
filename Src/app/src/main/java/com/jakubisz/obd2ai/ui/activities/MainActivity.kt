package com.jakubisz.obd2ai.ui.activities

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jakubisz.obd2ai.ui.theme.OBD2AITheme
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.control.PendingTroubleCodesCommand
import com.github.eltonvs.obd.command.control.PermanentTroubleCodesCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.jakubisz.obd2ai.helpers.BluetoothHelper
import com.jakubisz.obd2ai.ui.viewmodels.ConnectorViewModel
import com.jakubisz.obd2ai.ui.viewmodels.ConnectorViewModelFactory
import com.jakubisz.obd2ai.helpers.ObdHelper
import com.jakubisz.obd2ai.helpers.OpenAIService
import com.jakubisz.obd2ai.R
import com.jakubisz.obd2ai.ui.viewmodels.TestViewModel

class MainActivity : AppCompatActivity() { // ComponentActivity() {
    private lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var obdHelper: ObdHelper
    private lateinit var connector: ConnectorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initHelpers()
        connector = ViewModelProvider(this,
            ConnectorViewModelFactory(
                bluetoothHelper,
                obdHelper,
                OpenAIService()
            ))[ConnectorViewModel::class.java]
        setContentView(R.layout.activity_main)
    }

    private fun initHelpers() {
        bluetoothHelper = BluetoothHelper(this)
        obdHelper = ObdHelper(bluetoothHelper)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<out String>,
                                   grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (bluetoothHelper.resolvePermissionsResult(requestCode, grantResults)) {
            // Permissions granted. You can proceed with your Bluetooth operations.
            //setupObd()
        } else {
            // Permissions denied. Handle the feature's unavailability.
            //showPermissionsDeniedDialog()
        }
    }
}