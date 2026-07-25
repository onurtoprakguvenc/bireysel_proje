package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.SonIncelemeModel;

import java.util.List;

public class SonIncelemeAdapter extends RecyclerView.Adapter<SonIncelemeAdapter.ViewHolder> {

    private List<SonIncelemeModel> incelemeListesi;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SonIncelemeModel item);
        void onDeleteClick(int position);
    }

    public SonIncelemeAdapter(List<SonIncelemeModel> incelemeListesi, OnItemClickListener listener) {
        this.incelemeListesi = incelemeListesi;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_son_inceleme_kart, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<SonIncelemeModel> newList) {
        this.incelemeListesi = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SonIncelemeModel item = incelemeListesi.get(position);

        holder.txtTitle.setText(item.getBaslik());
        holder.txtDescription.setText(item.getAciklama());
        holder.txtTime.setText(item.getZaman());
        holder.txtBadge.setText(item.getTur());

        // Türüne göre ikon ve renk ayrımı
        if ("Kavram".equalsIgnoreCase(item.getTur())) {
            holder.imgIcon.setImageResource(R.drawable.ic_lightbulb);
            holder.txtBadge.setBackgroundResource(R.drawable.bg_badge_purple); // İstenirse turuncu badge eklenebilir
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_document);
            holder.txtBadge.setBackgroundResource(R.drawable.bg_badge_purple);
        }

        // Kart tıklaması
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });


        // Tekli silme (X) tıklaması
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return incelemeListesi != null ? incelemeListesi.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, btnRemove;
        TextView txtTitle, txtBadge, txtDescription, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgItemIcon);
            btnRemove = itemView.findViewById(R.id.btnRemoveSingleHistory);
            txtTitle = itemView.findViewById(R.id.txtItemTitle);
            txtBadge = itemView.findViewById(R.id.txtItemBadge);
            txtDescription = itemView.findViewById(R.id.txtItemDescription);
            txtTime = itemView.findViewById(R.id.txtItemTime);
        }
    }
}