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
import com.example.hadi_bakalm.noroplastite;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.MetinItem;
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
        // R.layout.kaydedilen_sey_buton_iste TASARIMI BAĞLANDI
        View view = LayoutInflater.from(context).inflate(R.layout.kaydedilen_sey_buton_iste, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        kaydedilenler item = itemList.get(position);
        if (item == null) return;

        if (holder.txtSavedTitle != null) holder.txtSavedTitle.setText(item.getTitle() != null ? item.getTitle() : "");
        if (holder.txtSavedDesc != null) holder.txtSavedDesc.setText(item.getDescription() != null ? item.getDescription() : "");
        if (holder.txtAddedTime != null) holder.txtAddedTime.setText(item.getAddedTime() != null ? item.getAddedTime() : "");

        updateBookmarkIconUI(holder.btnRemoveSave, item.isSaved());

        // KARTIN TAMAMINA TIKLANDIĞINDA
        holder.itemView.setOnClickListener(v -> handleItemClick(item));

        // 1. İNCELE BUTONUNA TIKLAMA OLAYI
        if (holder.btnInspect != null) {
            holder.btnInspect.setOnClickListener(v -> handleItemClick(item));
        }

        // 2. PAYLAŞ BUTONUNA TIKLAMA OLAYI
        if (holder.btnShare != null) {
            holder.btnShare.setOnClickListener(v -> {
                String shareBody = (item.getTitle() != null ? item.getTitle() : "") + "\n\n" +
                        ((item.getContent() != null && !item.getContent().isEmpty()) ? item.getContent() : (item.getDescription() != null ? item.getDescription() : ""));

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                context.startActivity(Intent.createChooser(shareIntent, "Metni Şununla Paylaş:"));
            });
        }

        // 3. KAYDET / KAYDEDİLENDEN ÇIKAR
        if (holder.btnRemoveSave != null) {
            holder.btnRemoveSave.setOnClickListener(v -> {
                boolean newSaveState = !item.isSaved();
                item.setSaved(newSaveState);

                updateBookmarkIconUI(holder.btnRemoveSave, newSaveState);

                AppDatabase db = AppDatabase.getInstance(context);
                new Thread(() -> {
                    try {
                        String type = item.getType() != null ? item.getType().toUpperCase() : "";

                        // A) KAVRAM İSE KAVRAM TABLOSUNU GÜNCELLE
                        if (type.contains("KAVRAM")) {
                            List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
                            ConceptItem_kavram matchedConcept = null;

                            if (allConcepts != null) {
                                for (ConceptItem_kavram concept : allConcepts) {
                                    if (concept.getTitle() != null && item.getTitle() != null &&
                                            concept.getTitle().equalsIgnoreCase(item.getTitle())) {
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
                                        "", "", "",
                                        newSaveState
                                );
                                db.conceptDao_kavram().insert(newConcept);
                            }
                        }
                        // B) METİN İSE METİNLER TABLOSUNU GÜNCELLE
                        else {
                            List<MetinItem> allMetinler = db.metinDao().getAllMetinler();
                            MetinItem matchedMetin = null;

                            if (allMetinler != null) {
                                for (MetinItem metin : allMetinler) {
                                    if (metin.getTitle() != null && item.getTitle() != null &&
                                            metin.getTitle().equalsIgnoreCase(item.getTitle())) {
                                        matchedMetin = metin;
                                        break;
                                    }
                                }
                            }

                            if (matchedMetin != null) {
                                matchedMetin.setSaved(newSaveState);
                                db.metinDao().update(matchedMetin);
                            } else {
                                MetinItem newMetin = new MetinItem(
                                        item.getTitle(),
                                        item.getContent() != null ? item.getContent() : item.getDescription(),
                                        "",
                                        newSaveState
                                );
                                db.metinDao().insert(newMetin);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                String msg = newSaveState ? "Kaydedilenlere eklendi" : "Kaydedilenlerden çıkarıldı";
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void handleItemClick(kaydedilenler item) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(item);
        } else {
            String type = item.getType() != null ? item.getType().toUpperCase() : "";
            Intent intent;

            if (type.contains("KAVRAM")) {
                intent = new Intent(context, noroplastite.class);
                intent.putExtra("KAVRAM_ADI", item.getTitle());
            } else {
                intent = new Intent(context, kisisel_metin_okuma_sayfa.class);
                intent.putExtra("TITLE", item.getTitle());
                intent.putExtra("DESCRIPTION", item.getDescription());
                intent.putExtra("CONTENT", item.getContent());
                intent.putExtra("READ_TIME", item.getAddedTime());
                intent.putExtra("CATEGORY", item.getCategory());
            }
            context.startActivity(intent);
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
        if (filteredList != null) {
            this.itemList = new java.util.ArrayList<>(filteredList);
        } else {
            this.itemList = new java.util.ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder {
        TextView txtSavedTitle, txtSavedDesc, txtAddedTime, btnInspect;
        ImageView btnRemoveSave, btnShare;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSavedTitle = itemView.findViewById(R.id.txtSavedTitle);
            txtSavedDesc = itemView.findViewById(R.id.txtSavedDesc);
            txtAddedTime = itemView.findViewById(R.id.txtAddedTime);
            btnInspect = itemView.findViewById(R.id.btnInspect);
            btnRemoveSave = itemView.findViewById(R.id.btnRemoveSave);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}