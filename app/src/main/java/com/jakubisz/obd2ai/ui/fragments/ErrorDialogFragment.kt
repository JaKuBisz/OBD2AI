package com.jakubisz.obd2ai.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(arguments?.getString("errorMessage"))
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    companion object {
        fun newInstance(errorMessage: String) = ErrorDialogFragment().apply {
            arguments = Bundle().apply {
                putString("errorMessage", errorMessage)
            }
        }
    }
}
