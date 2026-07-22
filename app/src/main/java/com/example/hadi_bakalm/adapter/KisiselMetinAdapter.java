package com.example.hadi_bakalm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.KisiselMetin;

import java.util.List;

public class KisiselMetinAdapter extends RecyclerView.Adapter<KisiselMetinAdapter.MetinViewHolder> {

    private List<KisiselMetin> metinListesi;

    public KisiselMetinAdapter(List<KisiselMetin> metinListesi) {
        this.metinListesi = metinListesi;
    }

    @NonNull
    @Override
    public MetinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itam_kisisel_metin_card, parent, false);
        return new MetinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetinViewHolder holder, int position) {
        KisiselMetin metin = metinListesi.get(position);
        holder.txtTitle.setText(metin.getBaslik());
        holder.txtDesc.setText(metin.getAciklama());
    }

    @Override
    public int getItemCount() {
        return metinListesi.size();
    }

    public static class MetinViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDesc;

        public MetinViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTextTitle);
            txtDesc = itemView.findViewById(R.id.txtTextDescription);
        }
    }
}