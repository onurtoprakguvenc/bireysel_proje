package com.example.hadi_bakalm.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.MetinItem;
import com.example.hadi_bakalm.kisisel_metin_okuma_sayfa;

import java.util.List;

@SuppressWarnings("unused")
public class KisiselMetinAdapter extends RecyclerView.Adapter<KisiselMetinAdapter.MetinViewHolder> {

    private final List<MetinItem> metinList;

    public KisiselMetinAdapter(List<MetinItem> metinList) {
        this.metinList = metinList;
    }

    @NonNull
    @Override
    public MetinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kaydedilen_sey_buton_iste, parent, false);
        return new MetinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetinViewHolder holder, int position) {
        if (metinList == null || position >= metinList.size()) return;

        MetinItem item = metinList.get(position);
        if (item == null) return;

        if (holder.txtTitle != null) {
            holder.txtTitle.setText(item.getTitle() != null ? item.getTitle() : "");
        }

        if (holder.txtDesc != null) {
            String summary = (item.getPersonalNote() != null && !item.getPersonalNote().trim().isEmpty())
                    ? item.getPersonalNote()
                    : (item.getContent() != null ? item.getContent() : "");
            holder.txtDesc.setText(summary);
        }

        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() != null) {
                Intent intent = new Intent(v.getContext(), kisisel_metin_okuma_sayfa.class);
                intent.putExtra("METIN_ID", item.getId());
                intent.putExtra("TITLE", item.getTitle());
                intent.putExtra("CONTENT", item.getContent());
                intent.putExtra("PERSONAL_NOTE", item.getPersonalNote());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return metinList != null ? metinList.size() : 0;
    }

    public static class MetinViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDesc;
        ImageView btnRemoveSave;

        public MetinViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtSavedTitle);
            txtDesc = itemView.findViewById(R.id.txtSavedDesc);
            btnRemoveSave = itemView.findViewById(R.id.btnRemoveSave);
        }
    }
}