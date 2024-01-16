package com.jakubisz.obd2ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakubisz.obd2ai.ui.viewmodels.ConnectorViewModel
import com.jakubisz.obd2ai.ui.adapters.ErrorOverviewRecyclerViewAdapter
import com.jakubisz.obd2ai.ui.activities.MainActivity
import com.jakubisz.obd2ai.R
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ErrorOverviewFragment : Fragment() {
    private lateinit var errorViewAdapter: ErrorOverviewRecyclerViewAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var statsTextView: TextView
    private var isDemo = false
    private val connectorViewModel: ConnectorViewModel by activityViewModels {
        (activity as MainActivity).defaultViewModelProviderFactory
    }
    private var errorCodes: List<DtpCodeDTO> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_error_overview, container, false)
        isDemo = ErrorOverviewFragmentArgs.fromBundle(requireArguments()).isDemo
        statsTextView = view.findViewById(R.id.textView_stats)
        //RecyclerView setup
        recyclerView = view.findViewById(R.id.view_error_overview)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        observeErrorCodes()

        showLoading(false)


        val btnRequestPermission = view.findViewById<Button>(R.id.button_analyze)
        btnRequestPermission.setOnClickListener {
            activity?.let { activity ->
                loadErrorCodes()
            }
        }

        return view
    }

    private fun loadErrorCodes() {
        showLoading(true)



        lifecycleScope.launch {
            if (isDemo) {
                delay(3000)
                //connectorViewModel.dtp.addAll(listOf("P0300", "P0420", "P0171", "P0128"))
                //connectorViewModel.assesDtpCodes()
                connectorViewModel.assesDtpCodesTest()
            }
            else{
            connectorViewModel.gatherDtpCodes()
            connectorViewModel.assesDtpCodes()
            }
        }
    }

    private fun observeErrorCodes() {
        connectorViewModel.dtpResults.observe(viewLifecycleOwner, Observer { dtpCodes ->
            val codes = dtpCodes.sortedBy { it.severity } ?: listOf()
            updateUI(codes)
        })
    }

    private fun updateUI(errorCodes: List<DtpCodeDTO>) {
        showLoading(errorCodes.isEmpty())
        if (errorCodes.isEmpty()) {
            return
        }
        var stats = "Low: ${errorCodes.count { it.severity == ErrorSeverity.LOW }}  " +
                "Medium: ${errorCodes.count { it.severity == ErrorSeverity.MEDIUM }}  " +
                "High: ${errorCodes.count { it.severity == ErrorSeverity.HIGH }}"
        statsTextView.text = stats
        errorViewAdapter.errorCodes = errorCodes
        errorViewAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        errorViewAdapter = ErrorOverviewRecyclerViewAdapter(errorCodes) { errorItem ->
            val action =
                ErrorOverviewFragmentDirections.actionErrorOverviewFragmentToErrorDetailFragment(
                    errorItem.errorCode
                )
            findNavController().navigate(action)

            recyclerView.adapter = errorViewAdapter

            showLoading(false)
        }
        recyclerView.adapter = errorViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    fun showErrorDialog(text: String) {
        val errorDialog = ErrorDialogFragment.newInstance(text)
        errorDialog.show(parentFragmentManager, "errorDialog")
    }

}