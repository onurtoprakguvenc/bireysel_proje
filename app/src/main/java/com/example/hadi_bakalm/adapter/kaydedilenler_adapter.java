package com.example.hadi_bakalm.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.hadi_bakalm.kisisel_metin_okuma_sayfa;
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

        // Model sınıfındaki verileri görünümlere bağlama
        holder.txtTagType.setText(item.getType());
        holder.txtTagCategory.setText("• " + item.getCategory());
        holder.txtSavedTitle.setText(item.getTitle());
        holder.txtSavedDesc.setText(item.getDescription());
        holder.txtAddedTime.setText(item.getAddedTime());

        // "İncele" butonuna tıklanınca Detay Sayfasına geçiş yapma
        holder.btnInspect.setOnClickListener(v -> {
            // "detay_sayfa" yerine projendeki detay activity sınıfının tam adını yazmalısın
             Intent intent = new Intent(context, kisisel_metin_okuma_sayfa.class);
             intent.putExtra("TITLE", item.getTitle());
             intent.putExtra("DESCRIPTION", item.getDescription());
             intent.putExtra("CATEGORY", item.getCategory());
             context.startActivity(intent);
        });

        // Silme butonuna tıklama
        holder.btnRemoveSave.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                showDeleteDialog(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private void showDeleteDialog(int position) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.kaydedilen_silme_uyari);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirmDelete = dialog.findViewById(R.id.btnConfirmDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            itemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemList.size());
            dialog.dismiss();
        });

        dialog.show();
    }

    public void filterList(List<kaydedilenler> filteredList) {
        this.itemList= filteredList; // Eğer sınıftaki liste değişken adın farklıysa (örn: list) onunla değiştir
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