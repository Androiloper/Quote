// PromiseAdapter.kt

package com.example.quotex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quotex.R
class PromiseAdapter(
    private var promises: List<Promise> = emptyList(),
    private val onItemClick: (Promise) -> Unit = {}
) : RecyclerView.Adapter<PromiseAdapter.PromiseViewHolder>() {

    class PromiseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.promise_title)
        private val verseView: TextView = itemView.findViewById(R.id.promise_verse)
        private val referenceView: TextView = itemView.findViewById(R.id.promise_reference)

        fun bind(promise: Promise, onClick: (Promise) -> Unit) {
            titleView.text = promise.title
            verseView.text = promise.verse
            referenceView.text = promise.reference
            itemView.setOnClickListener { onClick(promise) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromiseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.promise_item, parent, false)
        return PromiseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromiseViewHolder, position: Int) {
        holder.bind(promises[position], onItemClick)
    }

    override fun getItemCount() = promises.size

    fun updatePromises(newPromises: List<Promise>) {
        this.promises = newPromises
        notifyDataSetChanged()
    }
}
