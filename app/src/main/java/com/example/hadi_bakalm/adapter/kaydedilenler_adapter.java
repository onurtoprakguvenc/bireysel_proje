package com.example.hadi_bakalm.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.kisisel_metin_okuma_sayfa;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.kaydedilenler;

import java.util.List;

public class kaydedilenler_adapter extends RecyclerView.Adapter<kaydedilenler_adapter.SavedViewHolder> {

    private Context context;
    private List<kaydedilenler> itemList;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(kaydedilenler item);
    }

    public kaydedilenler_adapter(Context context, List<kaydedilenler> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.kaydedilen_sey_buton_iste, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        kaydedilenler item = itemList.get(position);

        if (holder.txtSavedTitle != null) holder.txtSavedTitle.setText(item.getTitle());
        if (holder.txtSavedDesc != null) holder.txtSavedDesc.setText(item.getDescription());
        if (holder.txtAddedTime != null) holder.txtAddedTime.setText(item.getAddedTime());

        // Duruma göre ikon rengi ayarla
        updateBookmarkIconUI(holder.btnRemoveSave, item.isSaved());

        // KARTIN TAMAMINA TIKLANDIĞINDA
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        });

        // 1. İNCELE BUTONUNA TIKLAMA OLAYI
        if (holder.btnInspect != null) {
            holder.btnInspect.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item);
                } else {
                    Intent intent = new Intent(context, kisisel_metin_okuma_sayfa.class);
                    intent.putExtra("TITLE", item.getTitle());
                    intent.putExtra("DESCRIPTION", item.getDescription());
                    context.startActivity(intent);
                }
            });
        }

        // 2. KAYDET / KAYDEDİLENDEN ÇIKAR
        if (holder.btnRemoveSave != null) {
            holder.btnRemoveSave.setOnClickListener(v -> {
                boolean newSaveState = !item.isSaved();
                item.setSaved(newSaveState);

                // İkon rengini anında güncelle
                updateBookmarkIconUI(holder.btnRemoveSave, newSaveState);

                AppDatabase db = AppDatabase.getInstance(context);
                new Thread(() -> {
                    List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
                    ConceptItem_kavram matchedConcept = null;

                    if (allConcepts != null) {
                        for (ConceptItem_kavram concept : allConcepts) {
                            if (concept.getTitle() != null && concept.getTitle().equalsIgnoreCase(item.getTitle())) {
                                matchedConcept = concept;
                                break;
                            }
                        }
                    }

                    if (matchedConcept != null) {
                        matchedConcept.setSaved(newSaveState);
                        db.conceptDao_kavram().update(matchedConcept);
                    } else {
                        ConceptItem_kavram newConcept = new ConceptItem_kavram(
                                item.getTitle(),
                                item.getDescription(),
                                "",
                                "",
                                "",
                                newSaveState
                        );
                        db.conceptDao_kavram().insert(newConcept);
                    }
                }).start();

                String msg = newSaveState ? "Kaydedilenlere eklendi" : "Kaydedilenlerden çıkarıldı";
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateBookmarkIconUI(ImageView imageView, boolean isSaved) {
        if (imageView != null) {
            int iconColor = isSaved ? Color.parseColor("#0F172A") : Color.parseColor("#CBD5E1");
            imageView.setColorFilter(iconColor);
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void filterList(List<kaydedilenler> filteredList) {
        this.itemList = filteredList;
        notifyDataSetChanged();
    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder {
        TextView txtSavedTitle, txtSavedDesc, txtAddedTime, btnInspect;
        ImageView btnRemoveSave;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSavedTitle = itemView.findViewById(R.id.txtSavedTitle);
            txtSavedDesc = itemView.findViewById(R.id.txtSavedDesc);
            txtAddedTime = itemView.findViewById(R.id.txtAddedTime);
            btnInspect = itemView.findViewById(R.id.btnInspect);
            btnRemoveSave = itemView.findViewById(R.id.btnRemoveSave);
        }
    }
}