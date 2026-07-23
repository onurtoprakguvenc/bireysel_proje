package com.example.hadi_bakalm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hadi_bakalm.R;
import java.util.List;

public class ana_sayfa_adapter extends RecyclerView.Adapter<ana_sayfa_adapter.ViewHolder> {

    private List<String> categories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String categoryName);
    }

    public ana_sayfa_adapter(List<String> categories, OnItemClickListener listener) {
        this.categories = categories;
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
        String category = categories.get(position);
        holder.txtTitle.setText(category);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtTitle, txtDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgCardIcon);
            txtTitle = itemView.findViewById(R.id.txtCardTitle); // XML'deki başlık ID'si
            txtDescription = itemView.findViewById(R.id.txtCardDescription); // XML'deki açıklama ID'si
        }
    }
}