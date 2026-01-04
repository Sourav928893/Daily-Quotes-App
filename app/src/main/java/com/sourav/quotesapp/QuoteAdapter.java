package com.sourav.quotesapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private final String[][] quotes;

    public QuoteAdapter(String[][] quotes) {
        this.quotes = quotes;
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        // Set quote text and author from data
        holder.tvQuote.setText(quotes[position][0]);
        holder.tvAuthor.setText("- " + quotes[position][1]);
    }

    @Override
    public int getItemCount() {
        return quotes != null ? quotes.length : 0;
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuote, tvAuthor;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding IDs from item_quote.xml
            tvQuote = itemView.findViewById(R.id.tvQuoteText);
            tvAuthor = itemView.findViewById(R.id.tvAuthorText);
        }
    }
}
