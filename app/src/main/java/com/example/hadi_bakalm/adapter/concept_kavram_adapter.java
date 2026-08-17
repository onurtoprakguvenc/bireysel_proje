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

    private final List<CategoryGroupModel> kategoriListesi = new ArrayList<>();
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public concept_kavram_adapter(List<CategoryGroupModel> kategoriListesi) {
        if (kategoriListesi != null) {
            this.kategoriListesi.addAll(kategoriListesi);
        }
    }

    /**
     * Dışarıdan gelen yeni verileri adapter'ın kendi listesine aktarır ve UI'ı yeniler.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<CategoryGroupModel> yeniListe) {
        this.kategoriListesi.clear();
        if (yeniListe != null) {
            this.kategoriListesi.addAll(yeniListe);
        }
        notifyDataSetChanged();
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
        holder.bind(kategoriListesi.get(position));
    }

    @Override
    public int getItemCount() {
        return kategoriListesi.size();
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtCategoryTitle;
        private final RecyclerView recyclerViewHorizontal;
        private final InnerCardAdapter innerAdapter;

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
            layoutManager.setInitialPrefetchItemCount(4);
            recyclerViewHorizontal.setLayoutManager(layoutManager);

            innerAdapter = new InnerCardAdapter(new ArrayList<>());
            recyclerViewHorizontal.setAdapter(innerAdapter);

            recyclerViewHorizontal.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
        }

        public void bind(CategoryGroupModel group) {
            if (group == null) return;
            txtCategoryTitle.setText(group.getKategoriBasligi());
            innerAdapter.updateData(group.getKavramlar());
        }
    }

    private static class InnerCardAdapter extends RecyclerView.Adapter<InnerCardAdapter.CardViewHolder> {
        private final List<concept_kavram_model> kavramlar = new ArrayList<>();

        public InnerCardAdapter(List<concept_kavram_model> initialKavramlar) {
            if (initialKavramlar != null) {
                this.kavramlar.addAll(initialKavramlar);
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        public void updateData(List<concept_kavram_model> newKavramlar) {
            this.kavramlar.clear();
            if (newKavramlar != null) {
                this.kavramlar.addAll(newKavramlar);
            }
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
            holder.bind(kavramlar.get(position));
        }

        @Override
        public int getItemCount() {
            return kavramlar.size();
        }

        static class CardViewHolder extends RecyclerView.ViewHolder {
            private final TextView txtTitle;
            private final TextView txtDesc;

            public CardViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtCardTitle);
                txtDesc = itemView.findViewById(R.id.txtCardDescription);
            }

            public void bind(concept_kavram_model item) {
                if (item == null) return;

                txtTitle.setText(item.getKavramAdi());
                txtDesc.setText(item.getAciklama());

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), noroplastite.class);
                    intent.putExtra("KAVRAM_ID", item.getId());
                    intent.putExtra("KAVRAM_ADI", item.getKavramAdi());
                    intent.putExtra("KAVRAM_ACIKLAMA", item.getAciklama());
                    intent.putExtra("KAVRAM_ICERIK", item.getIcerik());
                    v.getContext().startActivity(intent);
                });
            }
        }
    }
}