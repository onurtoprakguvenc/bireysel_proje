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

import java.util.List;

public class concept_kavram_adapter extends RecyclerView.Adapter<concept_kavram_adapter.KavramViewHolder> {

    private List<concept_kavram_model> kavramListesi;

    public concept_kavram_adapter(List<concept_kavram_model> kavramListesi) {
        this.kavramListesi = kavramListesi;
    }

    @NonNull
    @Override
    public KavramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Liste kart görünümü (XML adı kendi kart layout id'nize göre güncellenebilir)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kategori_card, parent, false);
        return new KavramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KavramViewHolder holder, int position) {
        concept_kavram_model kavram = kavramListesi.get(position);
        holder.txtKavramAdi.setText(kavram.getKavramAdi());
        holder.txtAciklama.setText(kavram.getAciklama());

        // Karta tıklama olayı
        holder.itemView.setOnClickListener(v -> {
            // İleride kavram detay/okuma sayfasına geçiş Intent'i buraya eklenecek
        });
    }

    @Override
    public int getItemCount() {
        return kavramListesi != null ? kavramListesi.size() : 0;
    }

    public static class KavramViewHolder extends RecyclerView.ViewHolder {
        TextView txtKavramAdi;
        TextView txtAciklama;

        public KavramViewHolder(@NonNull View itemView) {
            super(itemView);
            // Kart XML'indeki TextView ID'lerinize göre güncelleyebilirsiniz
            txtKavramAdi = itemView.findViewById(R.id.txtCardTitle);
            txtAciklama = itemView.findViewById(R.id.txtCardDescription);
        }
    }
}