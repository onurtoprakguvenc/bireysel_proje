package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hadi_bakalm.R;
import java.util.ArrayList;
import java.util.List;

public class ana_sayfa_adapter extends RecyclerView.Adapter<ana_sayfa_adapter.ViewHolder> {

    private final List<String> categories = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String categoryName);
    }

    public ana_sayfa_adapter(List<String> initialCategories, OnItemClickListener listener) {
        if (initialCategories != null) {
            this.categories.addAll(initialCategories);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kategori_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(categories.get(position), listener);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<String> filteredList) {
        this.categories.clear();
        if (filteredList != null) {
            this.categories.addAll(filteredList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtCardTitle);
        }

        public void bind(String categoryName, OnItemClickListener listener) {
            txtTitle.setText(categoryName);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(categoryName);
                }
            });
        }
    }
}