package com.example.quotex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuoteAdapter(private var quotes: List<Quote>) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quote_item, parent, false)
        // Must use MATCH_PARENT for both dimensions when used with ViewPager2
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        holder.bind(quote)
    }

    override fun getItemCount(): Int = quotes.size

    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.proverb_text)
        private val referenceView: TextView = itemView.findViewById(R.id.proverb_reference)

        fun bind(quote: Quote) {
            textView.text = quote.text
            referenceView.text = quote.reference
        }
    }

    fun updateQuotes(newQuotes: List<Quote>) {
        this.quotes = newQuotes
        notifyDataSetChanged()
    }
}
