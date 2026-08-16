package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class KisiselMetinAdapter extends RecyclerView.Adapter<KisiselMetinAdapter.MetinViewHolder> {

    private final List<MetinItem> metinList = new ArrayList<>();
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDeleteClick(MetinItem item, int position);
    }

    public KisiselMetinAdapter(List<MetinItem> initialList, OnItemDeleteListener deleteListener) {
        if (initialList != null) {
            this.metinList.addAll(initialList);
        }
        this.deleteListener = deleteListener;
    }

    public KisiselMetinAdapter(List<MetinItem> initialList) {
        this(initialList, null);
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
        holder.bind(metinList.get(position), deleteListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<MetinItem> newList) {
        this.metinList.clear();
        if (newList != null) {
            this.metinList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return metinList.size();
    }

    public static class MetinViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;
        private final TextView txtDesc;
        private final ImageView btnRemoveSave;

        public MetinViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtSavedTitle);
            txtDesc = itemView.findViewById(R.id.txtSavedDesc);
            btnRemoveSave = itemView.findViewById(R.id.btnRemoveSave);
        }

        public void bind(MetinItem item, OnItemDeleteListener deleteListener) {
            if (item == null) return;

            txtTitle.setText(item.getTitle() != null ? item.getTitle() : "");

            String personalNote = item.getPersonalNote();
            String content = item.getContent();
            String summary = (personalNote != null && !personalNote.trim().isEmpty())
                    ? personalNote
                    : (content != null ? content : "");
            txtDesc.setText(summary);

            itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                if (context != null) {
                    Intent intent = new Intent(context, kisisel_metin_okuma_sayfa.class);
                    intent.putExtra("METIN_ID", item.getId());
                    intent.putExtra("TITLE", item.getTitle());
                    intent.putExtra("CONTENT", item.getContent());
                    intent.putExtra("PERSONAL_NOTE", item.getPersonalNote());
                    context.startActivity(intent);
                }
            });

            if (btnRemoveSave != null) {
                btnRemoveSave.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            deleteListener.onDeleteClick(item, pos);
                        }
                    }
                });
            }
        }
    }
}