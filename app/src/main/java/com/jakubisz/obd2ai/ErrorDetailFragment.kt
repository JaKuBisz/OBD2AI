package com.jakubisz.obd2ai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity

class ErrorDetailFragment : Fragment() {
    private val connectorViewModel: ConnectorViewModel by activityViewModels {
        (activity as MainActivity).defaultViewModelProviderFactory
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SuggestionsRecycleViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_error_detail, container, false)
        recyclerView = view.findViewById(R.id.suggestedActionRecyclerView)

        // Inflate the layout for this fragment
        val errorCode = ErrorDetailFragmentArgs.fromBundle(requireArguments()).errorCode
        connectorViewModel.dtpResults.value?.find { it.errorCode == errorCode }?.let {
            renderErrorDetail(it, view)
        }

        return view
    }

    fun renderErrorDetail(dtpCodeDTO: DtpCodeDTO, view: View) {
        view.findViewById<TextView>(R.id.textViewErrorTitle).text = dtpCodeDTO.title
        view.findViewById<TextView>(R.id.textViewErrorCode).text = dtpCodeDTO.errorCode
        view.findViewById<TextView>(R.id.textViewErrorSeverity).text = "Severity: ${dtpCodeDTO.severity}"
        view.findViewById<TextView>(R.id.textViewErrorDetail).text = dtpCodeDTO.detail
        view.findViewById<TextView>(R.id.textViewErrorImplications).text = dtpCodeDTO.implications
        val color = ErrorSeverity.getColor(dtpCodeDTO.severity)
        view.findViewById<ImageView>(R.id.imageView_icon).setColorFilter(color)

        setupSuggestedActionsRecyclerView(view, dtpCodeDTO.suggestedActions)

    }

    private fun setupSuggestedActionsRecyclerView(view: View, suggestedActions: List<String>) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.suggestedActionRecyclerView)
        adapter = SuggestionsRecycleViewAdapter(suggestedActions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    fun setRecyclerViewHeightBasedOnChildren(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return

        var height = 0
        val childCount = adapter.itemCount
        for (i in 0 until childCount) {
            val child = recyclerView.getChildAt(i) ?: continue
            recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            height += child.measuredHeight
        }

        val layoutParams = recyclerView.layoutParams
        layoutParams.height = height
        recyclerView.layoutParams = layoutParams
        recyclerView.requestLayout()
    }

}