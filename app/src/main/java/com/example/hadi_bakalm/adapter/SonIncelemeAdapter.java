package com.example.hadi_bakalm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.SonIncelemeModel;

import java.util.List;

public class SonIncelemeAdapter extends RecyclerView.Adapter<SonIncelemeAdapter.ViewHolder> {

    private List<SonIncelemeModel> itemList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SonIncelemeModel item);
        void onDeleteClick(SonIncelemeModel item);
    }

    public SonIncelemeAdapter(List<SonIncelemeModel> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    public void updateList(List<SonIncelemeModel> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_son_inceleme_kart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SonIncelemeModel item = itemList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        ImageView btnRemove;
        TextView txtTitle;
        TextView txtBadge;
        TextView txtDescription;
        TextView txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgItemIcon);
            btnRemove = itemView.findViewById(R.id.btnDeleteItem);
            txtTitle = itemView.findViewById(R.id.txtItemTitle);
            txtBadge = itemView.findViewById(R.id.txtItemBadge);
            txtDescription = itemView.findViewById(R.id.txtItemDescription);
            txtTime = itemView.findViewById(R.id.txtItemTime);
        }

        public void bind(SonIncelemeModel item, OnItemClickListener listener) {
            if (item == null) return;

            if (txtTitle != null) {
                txtTitle.setText(item.getBaslik() != null ? item.getBaslik() : "");
            }

            if (txtDescription != null) {
                txtDescription.setText(item.getAciklama() != null ? item.getAciklama() : "");
            }

            if (txtBadge != null) {
                String tur = item.getTur();
                if ("Metin".equalsIgnoreCase(tur)) {
                    txtBadge.setText("Kişisel Metin");
                } else {
                    txtBadge.setText("Kavram");
                }
            }

            if (txtTime != null) {
                txtTime.setText("Son incelendi");
            }

            if (imgIcon != null) {
                if ("Metin".equalsIgnoreCase(item.getTur())) {
                    imgIcon.setImageResource(R.drawable.ic_document);
                } else {
                    imgIcon.setImageResource(R.drawable.ic_lightbulb);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });

            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(item);
                });
            }
        }
    }
}