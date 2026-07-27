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

public class horizontal_kavram_adapter extends RecyclerView.Adapter<horizontal_kavram_adapter.ViewHolder> {

    private List<concept_kavram_model> liste;

    public horizontal_kavram_adapter(List<concept_kavram_model> liste) {
        this.liste = liste;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kavram_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        concept_kavram_model item = liste.get(position);
        holder.txtConceptName.setText(item.getKavramAdi());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), noroplastite.class);
            intent.putExtra("KAVRAM_ADI", item.getKavramAdi());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return liste != null ? liste.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtConceptName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtConceptName = itemView.findViewById(R.id.txtConceptName);
        }
    }
}