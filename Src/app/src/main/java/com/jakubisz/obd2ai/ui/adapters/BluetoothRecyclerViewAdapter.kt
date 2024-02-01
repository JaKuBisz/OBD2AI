package com.jakubisz.obd2ai.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakubisz.obd2ai.R
import com.jakubisz.obd2ai.model.BluetoothDeviceDTO

class BluetoothRecyclerViewAdapter (
    private val devices: List<BluetoothDeviceDTO>,
    private val onItemClick: (BluetoothDeviceDTO) -> Unit
) : RecyclerView.Adapter<BluetoothRecyclerViewAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val deviceNameTextView: TextView = view.findViewById(R.id.deviceNameTextView)

        fun bind(device: BluetoothDeviceDTO, onItemClick: (BluetoothDeviceDTO) -> Unit) {
            deviceNameTextView.text = device.name ?: "Unknown Device"
            itemView.setOnClickListener { onItemClick(device) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], onItemClick)
    }

    override fun getItemCount(): Int = devices.size
}
