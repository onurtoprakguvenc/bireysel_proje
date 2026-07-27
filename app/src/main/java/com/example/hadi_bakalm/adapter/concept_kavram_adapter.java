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

    @Override
    public int getItemViewType(int position) {
        return liste.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == concept_kavram_model.TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_header, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_kategori_card, parent, false);
            return new ConceptViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        concept_kavram_model item = liste.get(position);

        if (getItemViewType(position) == concept_kavram_model.TYPE_CATEGORY) {
            CategoryViewHolder catHolder = (CategoryViewHolder) holder;
            catHolder.txtCategoryTitle.setText(item.getKategoriAdi());
        } else {
            ConceptViewHolder conceptHolder = (ConceptViewHolder) holder;
            conceptHolder.txtKavramTitle.setText(item.getKavramAdi());
            conceptHolder.txtKavramDesc.setText(item.getAciklama());

            conceptHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), noroplastite.class);
                // Tıklanan kavramın adını detay sayfasına taşıyoruz:
                intent.putExtra("KAVRAM_ADI", item.getKavramAdi());
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return liste != null ? liste.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryTitle;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryTitle = itemView.findViewById(R.id.txtCategoryTitle);
        }
    }

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