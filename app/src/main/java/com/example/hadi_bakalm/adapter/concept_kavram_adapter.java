package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.CategoryGroupModel;
import com.example.hadi_bakalm.model.concept_kavram_model;
import com.example.hadi_bakalm.noroplastite;

import java.util.ArrayList;
import java.util.List;

public class concept_kavram_adapter extends RecyclerView.Adapter<concept_kavram_adapter.RowViewHolder> {

    private final List<CategoryGroupModel> kategoriListesi;
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public concept_kavram_adapter(List<CategoryGroupModel> kategoriListesi) {
        this.kategoriListesi = kategoriListesi;
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kategori_row, parent, false);

        RowViewHolder holder = new RowViewHolder(view);
        holder.recyclerViewHorizontal.setRecycledViewPool(viewPool);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        CategoryGroupModel group = kategoriListesi.get(position);
        holder.txtCategoryTitle.setText(group.getKategoriBasligi());

        holder.innerAdapter.updateData(group.getKavramlar());
    }

    @Override
    public int getItemCount() {
        return kategoriListesi != null ? kategoriListesi.size() : 0;
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryTitle;
        RecyclerView recyclerViewHorizontal;
        InnerCardAdapter innerAdapter;

        @SuppressLint("ClickableViewAccessibility")
        public RowViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryTitle = itemView.findViewById(R.id.txtCategoryTitle);
            recyclerViewHorizontal = itemView.findViewById(R.id.recyclerViewHorizontalButtons);

            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            );
            recyclerViewHorizontal.setLayoutManager(layoutManager);

            innerAdapter = new InnerCardAdapter(new ArrayList<>());
            recyclerViewHorizontal.setAdapter(innerAdapter);

            // DİKEY RECYCLERVIEW'IN DOKUNMAYI GASP ETMESİNİ ENGELLEYEN KOD:
            recyclerViewHorizontal.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
        }
    }

    private static class InnerCardAdapter extends RecyclerView.Adapter<InnerCardAdapter.CardViewHolder> {
        private List<concept_kavram_model> kavramlar;

        public InnerCardAdapter(List<concept_kavram_model> kavramlar) {
            this.kavramlar = kavramlar;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void updateData(List<concept_kavram_model> newKavramlar) {
            this.kavramlar = newKavramlar;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_kavram_card_horizontal, parent, false);
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
            concept_kavram_model item = kavramlar.get(position);
            holder.txtTitle.setText(item.getKavramAdi());
            holder.txtDesc.setText(item.getAciklama());

            // Tıklama Olayı (Detay sayfasına tüm JSON verilerini eksiksiz taşır)
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), noroplastite.class);
                intent.putExtra("KAVRAM_ID", item.getId());
                intent.putExtra("KAVRAM_ADI", item.getKavramAdi());
                intent.putExtra("KAVRAM_ACIKLAMA", item.getAciklama());
                intent.putExtra("KAVRAM_ICERIK", item.getIcerik());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return kavramlar != null ? kavramlar.size() : 0;
        }

        static class CardViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle, txtDesc;

            public CardViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtCardTitle);
                txtDesc = itemView.findViewById(R.id.txtCardDescription);
            }
        }
    }
}