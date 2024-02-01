package com.jakubisz.obd2ai.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakubisz.obd2ai.model.BluetoothDeviceDTO
import com.jakubisz.obd2ai.model.DtpCodeDTO
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

interface IConnectorViewModel {
    val isBluetoothPermissionGranted: LiveData<Boolean>
    var dtp: MutableList<String>
    var pendingDtp: MutableList<String>
    var permanentDtp: MutableList<String>
    val allDtp: List<String>
    val dtpResults: MutableLiveData<List<DtpCodeDTO>>

    fun requestBluetoothPermissions(activity: Activity)
    fun enableBluetooth(activity: Activity)
    fun getAvailableDevices(): List<BluetoothDeviceDTO>
    fun connectToDevice(deviceAddress: String, onSuccess: (Pair<InputStream, OutputStream>) -> Unit, onFailure: (IOException) -> Unit)
    fun disconnectFromDevice()
    fun setupOBDConnection(deviceAddress: String)
    suspend fun gatherDtpCodes()
    fun assesDtpCodes()
}
