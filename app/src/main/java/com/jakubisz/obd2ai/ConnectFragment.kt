package com.jakubisz.obd2ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConnectFragment : Fragment() {
    private lateinit var bluetoothAdapter: BluetoothRecyclerViewAdapter
    private var deviceAdress: String = ""
    private val connectorViewModel: ConnectorViewModel by activityViewModels {
        (activity as MainActivity).defaultViewModelProviderFactory
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connect, container, false)

        val btnConnect = view.findViewById<Button>(R.id.button_connect)
        val btnDemo = view.findViewById<Button>(R.id.button_demo)
        val textSelected = view.findViewById<TextView>(R.id.textViewSelectedDev)
        val devices = connectorViewModel.getAvailableDevices()

        val recyclerView = view.findViewById<RecyclerView>(R.id.devicesView)
        val
        bluetoothAdapter = BluetoothRecyclerViewAdapter(devices) { device ->
            // Handle click
            deviceAdress = device.address // Get the device ID (address)
            textSelected.text = device.name
            btnConnect.visibility = View.VISIBLE
        }
        recyclerView.adapter = bluetoothAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)



        btnConnect.setOnClickListener {
            connectorViewModel.setupOBDConnection(deviceAdress)
            val action =
                ConnectFragmentDirections.actionConnectFragmentToErrorOverviewFragment(isDemo = false)
            findNavController().navigate(action)
        }
        btnDemo.setOnClickListener {
            val action =
                ConnectFragmentDirections.actionConnectFragmentToErrorOverviewFragment(isDemo = true)
            findNavController().navigate(action)
        }

        return view
    }
}