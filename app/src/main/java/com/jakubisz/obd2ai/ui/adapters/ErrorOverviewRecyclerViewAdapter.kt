package com.jakubisz.obd2ai.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakubisz.obd2ai.R
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity

class ErrorOverviewRecyclerViewAdapter(
    items: List<DtpCodeDTO>,
    private val onItemClick: (DtpCodeDTO) -> Unit
) : RecyclerView.Adapter<ErrorOverviewRecyclerViewAdapter.ErrorCodeViewHolder>() {
    var errorCodes = items
    class ErrorCodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconImageView: ImageView = view.findViewById(R.id.imageView_icon)
        private val titleTextView: TextView = view.findViewById(R.id.title)
        private val detailTextView: TextView = view.findViewById(R.id.detail)

        fun bind(item: DtpCodeDTO, onItemClick: (DtpCodeDTO) -> Unit) {
            titleTextView.text = item.title
            detailTextView.text = item.detail
            // Set color based on severity

            val color = ErrorSeverity.getColor(item.severity)
            iconImageView.setColorFilter(color)
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorCodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_error_overview_layout, parent, false)
        return ErrorCodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ErrorCodeViewHolder, position: Int) {
        holder.bind(errorCodes[position], onItemClick)
    }

    override fun getItemCount(): Int = errorCodes.size
    
}