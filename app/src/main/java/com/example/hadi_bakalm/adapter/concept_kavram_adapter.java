package com.example.hadi_bakalm.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.concept_kavram_model;
import com.example.hadi_bakalm.noroplastite;

import java.util.List;

public class concept_kavram_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<concept_kavram_model> liste;

    public concept_kavram_adapter(List<concept_kavram_model> liste) {
        this.liste = liste;
    }

    // 1. Verinin türünü (Başlık mı, Kart mı?) belirliyoruz
    @Override
    public int getItemViewType(int position) {
        return liste.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == concept_kavram_model.TYPE_CATEGORY) {
            // Kategori başlığı görünümü
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_header, parent, false);
            return new CategoryViewHolder(view);
        } else {
            // Normal kavram kartı görünümü
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_kategori_card, parent, false);
            return new ConceptViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        concept_kavram_model item = liste.get(position);

        if (getItemViewType(position) == concept_kavram_model.TYPE_CATEGORY) {
            // Kategori başlığı verisini bas
            CategoryViewHolder catHolder = (CategoryViewHolder) holder;
            catHolder.txtCategoryTitle.setText(item.getKategoriAdi());
        } else {
            // Kavram kartı verisini bas
            ConceptViewHolder conceptHolder = (ConceptViewHolder) holder;
            conceptHolder.txtKavramTitle.setText(item.getKavramAdi());
            conceptHolder.txtKavramDesc.setText(item.getAciklama());

            // Kart tıklama olayı
            conceptHolder.itemView.setOnClickListener(v -> {
                if (item.getKavramAdi() != null && item.getKavramAdi().equalsIgnoreCase("Nöroplastisite")) {
                    Intent intent = new Intent(v.getContext(), noroplastite.class);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return liste != null ? liste.size() : 0;
    }

    // ViewHolder 1: Kategori Başlığı İçin
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryTitle;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryTitle = itemView.findViewById(R.id.txtCategoryTitle);
        }
    }

    // ViewHolder 2: Kavram Kartı İçin
    public static class ConceptViewHolder extends RecyclerView.ViewHolder {
        TextView txtKavramTitle;
        TextView txtKavramDesc;

        public ConceptViewHolder(@NonNull View itemView) {
            super(itemView);
            txtKavramTitle = itemView.findViewById(R.id.txtCardTitle);
            txtKavramDesc = itemView.findViewById(R.id.txtCardDescription);
        }
    }
}