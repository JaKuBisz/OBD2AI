package com.jakubisz.obd2ai

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.model.BluetoothDeviceDTO
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectorViewModel(
    private val bluetoothHelper: BluetoothHelper,
    private val obdHelper: ObdHelper,
    private val openAI: OpenAIService
) : ViewModel() {
    // Function to request Bluetooth permissions

    val isBluetoothPermissionGranted: LiveData<Boolean> = bluetoothHelper.isBluetoothPermissionGranted
    var dtp : MutableList<String> = mutableListOf()
    var pendingDtp : MutableList<String> = mutableListOf()
    var permanentDtp : MutableList<String> = mutableListOf()
    val allDtp = dtp + pendingDtp + permanentDtp
    val dtpResults = MutableLiveData<List<DtpCodeDTO>>()


    //---------------------------------Bluetooth---------------------------------

    // Function to request Bluetooth permissions
    fun requestBluetoothPermissions(activity: Activity) {
        bluetoothHelper.requestPermissions(activity)
    }

    // Function to enable Bluetooth
    fun enableBluetooth(activity: Activity) {
        viewModelScope.launch {
            try {
                bluetoothHelper.enableBluetooth()
            } catch (e: SecurityException) {
                // Handle if permissions are not granted
                bluetoothHelper.requestPermissions(activity)
            }
        }
    }

    // Function to get available devices
    fun getAvailableDevices(): List<BluetoothDeviceDTO> {
        val dummyDevices = mutableListOf<BluetoothDeviceDTO>()

        //Real data
        dummyDevices.addAll(bluetoothHelper.getAvailableDevices())
        // Dummy data
        for (i in 1..15) {
            dummyDevices.add(
                BluetoothDeviceDTO(
                    name = "Dummy Device $i",
                    address = "00:00:00:00:00:${i.toString().padStart(2, '0')}"
                )
            )
        }
        return dummyDevices
        //        return bluetoothHelper.getAvailableDevices()
    }

    // Function to connect to a device
    fun connectToDevice(deviceAddress: String, onSuccess: (Pair<InputStream, OutputStream>) -> Unit, onFailure: (IOException) -> Unit) {
        viewModelScope.launch {
            try {
                val connection = bluetoothHelper.connectToDevice(deviceAddress)
                onSuccess(connection)
            } catch (e: IOException) {
                onFailure(e)
            }
        }
    }

    // Function to disconnect from a device
    fun disconnectFromDevice() {
        bluetoothHelper.disconnectFromDevice()
    }

    //---------------------------------OBD---------------------------------

    // Function to setup OBD connection
    fun setupOBDConnection(deviceAddress: String) {
        viewModelScope.launch { obdHelper.setupObd(deviceAddress) }
    }

    // Function to get DTP codes from OBD sensor
    suspend fun gatherDtpCodes()
    {
        dtp = obdHelper.getDtpCodes().toMutableList()
        pendingDtp = obdHelper.getPendingDtpCodes().toMutableList()
        permanentDtp = obdHelper.getPermanentDtpCodes().toMutableList()
    }

    //---------------------------------OpenAI---------------------------------

    // Function to get DTP codes
    fun assesDtpCodes() {
        viewModelScope.launch {
            try {
                //val results = listOf(openAI.getDtpCodeAssessment("P0300"))
                val results = dtp.map { openAI.getDtpCodeAssessment(it) }

                dtpResults.postValue(results)
            } catch (e: SecurityException) {
                Log.e("ConnectorViewModel", "Error in assesDtpCodes: ${e.message}")
                dtpResults.postValue(emptyList())
            }
        }
    }

    //---------------------------------Tests---------------------------------

    fun assesDtpCodesTest() {
        val results = mutableListOf<DtpCodeDTO>()
        results.add(
            DtpCodeDTO(
            errorCode = "P0300",
            severity = ErrorSeverity.MEDIUM, // Assuming 2 corresponds to MEDIUM
            title = "Random/Multiple Cylinder Misfire Detected",
            detail = "This error code indicates that the engine's control module has detected that one or more cylinders are misfiring randomly.",
            implications = "Continued driving with this issue could cause damage to the catalytic converter, engine, or other components over time.",
            suggestedActions = listOf("Check for loose or damaged ignition system components", "Inspect the fuel system for issues", "Examine the air intake for blockages or leaks")
        )
        )
        results.add(
            DtpCodeDTO(
            errorCode = "P0420",
            severity = ErrorSeverity.MEDIUM,
            title = "Catalyst System Efficiency Below Threshold",
            detail = "This error indicates that the oxygen levels in the exhaust are not as expected, suggesting inefficiency in the catalyst system.",
            implications = "This may lead to increased emissions and may affect the vehicle's fuel efficiency. It should be addressed as soon as possible.",
            suggestedActions = listOf("Check the catalytic converter", "Inspect the oxygen sensors", "Examine exhaust system for leaks")
        )
        )
        results.add(
            DtpCodeDTO(
            errorCode = "P0171",
            severity = ErrorSeverity.LOW,
            title = "System Too Lean (Bank 1)",
            detail = "This error code indicates that the oxygen sensor has detected an air-fuel mixture that is too lean on bank 1 of the engine.",
            implications = "Continued driving with this condition could lead to increased emissions and potential damage to the engine.",
            suggestedActions = listOf("Inspect for vacuum leaks", "Check the air intake for restrictions or leaks", "Test the fuel pressure regulator")
        )
        )
        results.add(
            DtpCodeDTO(
            errorCode = "P0128",
            severity = ErrorSeverity.LOW,
            title = "Coolant Thermostat (Coolant Temperature Below Thermostat Regulating Temperature)",
            detail = "This error code indicates that the engine does not reach expected temperature within a specified time period, indicating a potential issue with the vehicle's cooling system.",
            implications = "Driving with this issue may lead to reduced fuel economy, increased emissions, and potential engine damage.",
            suggestedActions = listOf("Check the coolant level and condition", "Inspect the thermostat for proper operation", "Test the engine coolant temperature sensor")
        )
        )
        results.add(
            DtpCodeDTO(
            errorCode = "P0504",
            severity = ErrorSeverity.LOW,
            title = "Vehicle Speed Sensor Circuit Malfunction",
            detail = "These error codes indicate a malfunction in the vehicle speed sensor circuit, which is responsible for monitoring the vehicle's speed and sending signals to the engine control module.",
            implications = "Issues with the vehicle speed sensor can affect the vehicle's performance, fuel efficiency, and the operation of the transmission.",
            suggestedActions =listOf("Inspect the vehicle speed sensor for damage or corrosion", "Check the wiring and connectors related to the speed sensor", "Test the speed sensor output with a scan tool")
        )
        )
        results.add(
            DtpCodeDTO(
            errorCode = "P0532",
            severity = ErrorSeverity.MEDIUM,
            title = "A/C Refrigerant Pressure Sensor Circuit Low Input and Knock Sensor 2 Circuit Low Input (Bank 2)",
            detail = "These error codes indicate issues with the A/C refrigerant pressure sensor circuit and the knock sensor 2 circuit (Bank 2) in the engine.",
            implications = "The A/C refrigerant pressure sensor issue may lead to improper operation of the A/C system, while the knock sensor issue could affect engine performance and emissions.",
            suggestedActions = listOf("Inspect the A/C refrigerant pressure sensor and its wiring", "Check the knock sensor and related wiring (Bank 2)", "Test the circuits for proper voltage and connectivity")
        )
        )
        results.add(
            DtpCodeDTO(
            errorCode = "P0136, P0340",
            severity = ErrorSeverity.MEDIUM,
            title = "Oxygen Sensor Circuit Malfunction (Bank 1, Sensor 2) and Camshaft Position Sensor 'A' Circuit (Bank 1 or Single Sensor)",
            detail = "These error codes indicate malfunctions in the oxygen sensor circuit for bank 1, sensor 2, and the camshaft position sensor circuit for bank 1 or a single sensor.",
            implications = "Issues with the oxygen sensor can lead to increased emissions and reduced fuel efficiency, while camshaft position sensor problems may affect engine performance and timing.",
            suggestedActions = listOf("Inspect the oxygen sensor and its wiring (Bank 1, Sensor 2)", "Check the camshaft position sensor and related wiring", "Test the sensor circuits for proper voltage and signal")
        )
        )
        dtpResults.postValue(results)
    }
}
