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

    private final List<SonIncelemeModel> incelemeListesi = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SonIncelemeModel item);
        void onDeleteClick(SonIncelemeModel item);
    }

    public SonIncelemeAdapter(List<SonIncelemeModel> initialList, OnItemClickListener listener) {
        if (initialList != null) {
            this.incelemeListesi.addAll(initialList);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_son_inceleme_kart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(incelemeListesi.get(position), listener);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<SonIncelemeModel> newList) {
        this.incelemeListesi.clear();
        if (newList != null) {
            this.incelemeListesi.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unused")
    public void filterList(List<SonIncelemeModel> filteredList) {
        updateList(filteredList);
    }

    @Override
    public int getItemCount() {
        return incelemeListesi.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgIcon;
        private final ImageView btnRemove;
        private final TextView txtTitle;
        private final TextView txtBadge;
        private final TextView txtDescription;
        private final TextView txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgItemIcon);
            btnRemove = itemView.findViewById(R.id.btnRemoveSingleHistory);
            txtTitle = itemView.findViewById(R.id.txtItemTitle);
            txtBadge = itemView.findViewById(R.id.txtItemBadge);
            txtDescription = itemView.findViewById(R.id.txtItemDescription);
            txtTime = itemView.findViewById(R.id.txtReadTime);
        }

        @SuppressLint("SetTextI18n")
        public void bind(SonIncelemeModel item, OnItemClickListener listener) {
            if (item == null) return;

            txtTitle.setText(item.getBaslik() != null ? item.getBaslik() : "");
            txtDescription.setText(item.getAciklama() != null ? item.getAciklama() : "");
            txtTime.setText(item.getZaman() != null ? item.getZaman() : "");

            txtBadge.setBackgroundResource(R.drawable.bg_badge_purple);

            String tur = item.getTur() != null ? item.getTur().trim() : "";
            if ("KAVRAM".equalsIgnoreCase(tur)) {
                txtBadge.setText("Kavram");
                imgIcon.setImageResource(R.drawable.ic_lightbulb);
            } else {
                txtBadge.setText("Metin");
                imgIcon.setImageResource(R.drawable.ic_document);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });

            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(item);
                });
            }
        }
    }
}