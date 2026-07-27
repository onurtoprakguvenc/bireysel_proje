package com.example.hadi_bakalm.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.kaydet_ana_sayfa;
import com.example.hadi_bakalm.model.kaydedilenler;

import java.util.List;

public class kaydedilenler_adapter extends RecyclerView.Adapter<kaydedilenler_adapter.SavedViewHolder> {

    private Context context;
    private List<kaydedilenler> itemList;

    public kaydedilenler_adapter(Context context, List<kaydedilenler> itemList) {
        this.context = context;
        this.itemList = itemList;
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
        if (holder.txtTagType != null) holder.txtTagType.setText(item.getType());
        if (holder.txtTagCategory != null) holder.txtTagCategory.setText(item.getCategory());

        // Kaydedilenlerden Çıkar Butonu Tıklama Olayı
        if (holder.btnRemoveSave != null) {
            holder.btnRemoveSave.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                showDeleteDialog(currentPosition);
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    private void showDeleteDialog(int position) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.kaydedilen_silme_uyari);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirmDelete = dialog.findViewById(R.id.btnConfirmDelete);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirmDelete != null) {
            btnConfirmDelete.setOnClickListener(v -> {
                if (position >= 0 && position < itemList.size()) {
                    kaydedilenler itemToDelete = itemList.get(position);

                    // 1. ROOM VERİ TABANINDA İLGİLİ KAYDIN isSaved DURUMUNU FALSE YAPMA
                    AppDatabase db = AppDatabase.getInstance(context);
                    new Thread(() -> {
                        List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
                        if (allConcepts != null) {
                            for (ConceptItem_kavram concept : allConcepts) {
                                boolean isSameId = String.valueOf(concept.getId()).equals(itemToDelete.getId());
                                boolean isSameTitle = concept.getTitle() != null && concept.getTitle().equalsIgnoreCase(itemToDelete.getTitle());

                                if (isSameId || isSameTitle) {
                                    concept.setSaved(false);
                                    db.conceptDao_kavram().update(concept);
                                    break;
                                }
                            }
                        }
                    }).start();

                    // 2. LİSTEDEN VE ARAYÜZDEN KALDIRMA
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, itemList.size());
                }

                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public void filterList(List<kaydedilenler> filteredList) {
        this.itemList = filteredList;
        notifyDataSetChanged();
    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder {
        TextView txtTagType, txtTagCategory, txtSavedTitle, txtSavedDesc, txtAddedTime, btnInspect;
        ImageView btnRemoveSave;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTagType = itemView.findViewById(R.id.txtTagType);
            txtTagCategory = itemView.findViewById(R.id.txtTagCategory);
            txtSavedTitle = itemView.findViewById(R.id.txtSavedTitle);
            txtSavedDesc = itemView.findViewById(R.id.txtSavedDesc);
            txtAddedTime = itemView.findViewById(R.id.txtAddedTime);
            btnInspect = itemView.findViewById(R.id.btnInspect);
            btnRemoveSave = itemView.findViewById(R.id.btnRemoveSave);
        }
    }
}