package com.jakubisz.obd2ai.ui.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakubisz.obd2ai.helpers.BluetoothHelper
import com.jakubisz.obd2ai.helpers.ObdHelper
import com.jakubisz.obd2ai.helpers.OpenAIService
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
        results.add( DtpCodeDTO( errorCode = "P0300", severity = ErrorSeverity.HIGH, title = "Random/Multiple Cylinder Misfire Detected", detail = "This code indicates that one or more engine cylinders are misfiring, causing the engine to run unevenly. This could be due to several factors such as faulty spark plugs, ignition coil, fuel injectors, camshaft/crankshaft position sensors, or issues with the air-fuel mixture.", implications = "If left unresolved, this problem could lead to significant reduction in engine performance, fuel economy, and an increase in harmful emissions. In severe cases, it may cause damage to the catalytic converter, a major and expensive component of the vehicle's emission control system.", suggestedActions = listOf("Check and replace spark plugs and wires if necessary", "Test ignition coils and replace if necessary", "Inspect fuel injectors and replace if necessary", "Test camshaft and crankshaft position sensors", "Check for vacuum leaks and repair if detected") ) )
        results.add( DtpCodeDTO( errorCode = "P0420", severity = ErrorSeverity.HIGH, title = "Catalyst System Efficiency Below Threshold", detail = "This code suggests that the efficiency of the catalytic converter is below the manufacturer-defined threshold. This condition may be caused by a malfunctioning catalytic converter, oxygen sensor, air leaks in the exhaust system, or issues with the fuel injection system.", implications = "Failure to resolve this issue promptly may result in increased tailpipe emissions, failed emission test, decreased fuel economy, and potential engine damage in the long run.", suggestedActions = listOf("Check and replace the catalytic converter if necessary", "Test oxygen sensor and replace if necessary", "Inspect for air leaks in the exhaust system and repair", "Check the fuel injection system for any faults") ) )
        results.add( DtpCodeDTO( errorCode = "P0171", severity = ErrorSeverity.HIGH, title = "System Too Lean", detail = "This error code means the engine's air-fuel mixture has too much air (oxygen) and not enough fuel. This could happen due to a vacuum leak, a faulty Mass Air Flow (MAF) sensor, fuel pressure problems, or a defective oxygen sensor.", implications = "If left unresolved, it could lead to poor vehicle performance, higher emissions, and potential engine damage. Thus, it requires immediate attention.", suggestedActions = listOf("Inspect for vacuum leaks and repair if detected", "Check the MAF sensor and clean or replace if necessary", "Test fuel pressure and rectify any issues", "Check the oxygen sensor and replace if necessary") ) )
        results.add( DtpCodeDTO( errorCode = "P0136", severity = ErrorSeverity.HIGH, title = "Oxygen Sensor Circuit Malfunction", detail = "The code indicates a malfunction within the circuit of oxygen sensor no.2, usually located in the exhaust system. Issues could result from a faulty sensor, damaged wires, or loose electrical connections.", implications = "If unresolved, this fault could lead to reduced fuel efficiency, sub-optimal engine performance, and an increase in harmful exhaust emissions. It's crucial to fix it to prevent possible engine damage.", suggestedActions = listOf("Check and replace the oxygen sensor if needed", "Inspect wiring and connections to the sensor", "If the check engine light persists, consider diagnosing the engine control unit (ECU)") ) )
        results.add( DtpCodeDTO( errorCode = "P0340", severity = ErrorSeverity.HIGH, title = "Camshaft Position Sensor Circuit Malfunction", detail = "The code signals an issue within the 'A' circuit of the camshaft position sensor, which monitors the rotational position of the camshaft. Causes could be a faulty sensor, wiring issues, or a broken timing belt/chain.", implications = "Engine performance could be significantly compromised, potentially leading to engine stalling, hard starting, or engine damage in severe cases. Prompt attention is needed.", suggestedActions = listOf("Check and replace the camshaft position sensor if necessary", "Inspect and repair wiring issues", "Check the timing belt/chain and replace if necessary") ) )
        results.add( DtpCodeDTO( errorCode = "P1523", severity = ErrorSeverity.MEDIUM, title = "Intake Manifold Runner Control Circuit Malfunction", detail = "This code denotes an error in the Intake Manifold Runner Control (IMRC) circuit. The issue could be a result of a faulty IMRC solenoid or actuator, or a problem in the relevant wiring harness or connectors.", implications = "Vehicle performance could be negatively affected, including power loss, stalled engine, or increased fuel emissions. To prevent further damage to the engine, repairs should be done promptly.", suggestedActions = listOf("Inspect and replace the IMRC solenoid if necessary", "Check the IMRC actuator", "Inspect the wiring harness and the connectors", "Check for related trouble codes that could suggest more specific issues") ) )
        results.add( DtpCodeDTO( errorCode = "P0128", severity = ErrorSeverity.MEDIUM, title = "Coolant Thermostat Malfunction", detail = "This error code indicates a potential defect with the coolant thermostat, which regulates the engine's operating temperature. It could mean that the thermostat is stuck in the open position, or the engine coolant temperature sensor is faulty.", implications = "Ignoring this problem could lead to increased fuel consumption, poor engine performance, and potential engine damage due to overcooling or overheating.", suggestedActions = listOf("Inspect and replace the thermostat if necessary", "Check the engine coolant temperature sensor and replace if needed", "Check the coolant level and top off if low") ) )
        results.add( DtpCodeDTO( errorCode = "P0504", severity = ErrorSeverity.MEDIUM, title = "Brake Switch 'A'/'B' Correlation", detail = "This error code means there is a discrepancy between the 'A' and 'B' brake switches signals. Possible causes include a faulty brake switch, a blown fuse, or wiring issues.", implications = "Failure to address this issue could influence several vehicle systems such as cruise control or anti-lock braking system (ABS), affecting driving safety. Therefore, timely action is recommended.", suggestedActions = listOf("Check the brake switch and replace if necessary", "Inspect the brake system's fuses and replace any that are blown", "Check for wiring issues in the brake system and fix if detected") ) )
        results.add( DtpCodeDTO( errorCode = "P0532", severity = ErrorSeverity.MEDIUM, title = "Air Conditioning Refrigerant Pressure Sensor Low Input", detail = "This code indicates that the input from the air conditioning refrigerant pressure sensor is abnormally low. It's most likely due to low refrigerant levels in the AC system caused by leaks or a faulty sensor.", implications = "Your car’s air conditioning may work inefficiently, reducing riding comfort. Prolonged disregard to this issue could lead to the failure of the air conditioning system.", suggestedActions = listOf("Refill AC refrigerant levels", "Check the refrigerant pressure sensor", "Inspect for leaks in the AC system and repair") ) )
        dtpResults.postValue(results)
    }
}
