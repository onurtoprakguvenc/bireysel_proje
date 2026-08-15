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

import java.util.ArrayList;
import java.util.List;

public class SonIncelemeAdapter extends RecyclerView.Adapter<SonIncelemeAdapter.ViewHolder> {

    private List<SonIncelemeModel> incelemeListesi;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SonIncelemeModel item);
        void onDeleteClick(SonIncelemeModel item);
    }

    public SonIncelemeAdapter(List<SonIncelemeModel> incelemeListesi, OnItemClickListener listener) {
        this.incelemeListesi = incelemeListesi != null ? new ArrayList<>(incelemeListesi) : new ArrayList<>();
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
        if (this.incelemeListesi == null) {
            this.incelemeListesi = new ArrayList<>();
        }
        this.incelemeListesi.clear();
        if (newList != null) {
            this.incelemeListesi.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SonIncelemeModel item = incelemeListesi.get(position);
        if (item == null) return;

        if (holder.txtTitle != null) holder.txtTitle.setText(item.getBaslik() != null ? item.getBaslik() : "");
        if (holder.txtDescription != null) holder.txtDescription.setText(item.getAciklama() != null ? item.getAciklama() : "");
        if (holder.txtTime != null) holder.txtTime.setText(item.getZaman() != null ? item.getZaman() : "");

        String tur = item.getTur() != null ? item.getTur().trim() : "";

        // Ortak rozet arka planı
        if (holder.txtBadge != null) {
            holder.txtBadge.setBackgroundResource(R.drawable.bg_badge_purple);
        }

        if ("KAVRAM".equalsIgnoreCase(tur)) {
            if (holder.txtBadge != null) holder.txtBadge.setText("Kavram");
            if (holder.imgIcon != null) holder.imgIcon.setImageResource(R.drawable.ic_lightbulb);
        } else {
            if (holder.txtBadge != null) holder.txtBadge.setText("Metin");
            if (holder.imgIcon != null) holder.imgIcon.setImageResource(R.drawable.ic_document);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        if (holder.btnRemove != null) {
            holder.btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unused")
    public void filterList(List<SonIncelemeModel> filteredList) {
        updateList(filteredList);
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
            txtTime = itemView.findViewById(R.id.txtReadTime);
        }
    }
}