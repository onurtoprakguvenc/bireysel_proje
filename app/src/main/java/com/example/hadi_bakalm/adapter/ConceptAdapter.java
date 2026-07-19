package com.example.hadi_bakalm.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.KavramDetayActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.Concept;

import java.util.List;

public class ConceptAdapter extends RecyclerView.Adapter<ConceptAdapter.ConceptViewHolder> {

    // Intent ile detay ekranına kavram adını taşırken kullanılacak anahtar (key)
    public static final String EXTRA_CONCEPT_NAME = "concept_name";

    private final List<Concept> conceptList;

    public ConceptAdapter(List<Concept> conceptList) {
        this.conceptList = conceptList;
    }

    @NonNull
    @Override
    public ConceptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kavram_karti, parent, false);
        return new ConceptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConceptViewHolder holder, int position) {
        Concept concept = conceptList.get(position);
        holder.tvKavramAdi.setText(concept.getName());

        // Karta tıklanınca: tek KavramDetayActivity'yi aç, hangi kavram olduğunu Intent ile gönder
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, KavramDetayActivity.class);
            intent.putExtra(EXTRA_CONCEPT_NAME, concept.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conceptList.size();
    }

    public static class ConceptViewHolder extends RecyclerView.ViewHolder {

        TextView tvKavramAdi;

        public ConceptViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKavramAdi = itemView.findViewById(R.id.tvKavramAdi);
        }
    }
}
