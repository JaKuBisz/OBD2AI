package com.jakubisz.obd2ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.jakubisz.obd2ai.ui.viewmodels.ConnectorViewModel
import com.jakubisz.obd2ai.ui.activities.MainActivity
import com.jakubisz.obd2ai.R

class PermissionsFragment : Fragment() {
    private val connectorViewModel: ConnectorViewModel by activityViewModels {
        (activity as MainActivity).defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_permissions, container, false)
        //val bluetoothViewModel = TestViewModel(bluetoothHelper, obdHelper, OpenAIService())

        val btnRequestPermission = view.findViewById<Button>(R.id.button_grant)
        connectorViewModel.isBluetoothPermissionGranted.observe(viewLifecycleOwner, Observer { data ->
            if (data)
            {
                findNavController().navigate(R.id.action_permissions_to_connectFragment)
            }
            else
            {
                val errorDialog = ErrorDialogFragment.newInstance("Required Bluetooth permissions were not granted")
                errorDialog.show(parentFragmentManager, "errorDialog")
            }
        })
        btnRequestPermission.setOnClickListener {
            activity?.let { activity ->
                connectorViewModel.requestBluetoothPermissions(activity)
            }
        }

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        findNavController().navigate(R.id.action_permissions_to_connectFragment)
    }
}