package com.jakubisz.obd2ai

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionsRecycleViewAdapter(private val actions: List<String>) :
RecyclerView.Adapter<SuggestionsRecycleViewAdapter.ActionViewHolder>() {

    class ActionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_error_detail_suggestions_layout, parent, false)) {
        private var textView: TextView = itemView.findViewById(R.id.textViewSuggestedAction)
        fun bind(action: String) {
            textView.text = action
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ActionViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val action = actions[position]
        holder.bind(action)
    }

    override fun getItemCount() = actions.size
}
