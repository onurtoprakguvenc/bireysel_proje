package com.example.hadi_bakalm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.NoteModel;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NoteModel note, int position);
        void onItemLongClick(NoteModel note, int position); // YENİ EKLENDİ
    }

    private Context context;
    private List<NoteModel> noteList;
    private List<NoteModel> filteredList;
    private OnItemClickListener listener;

    public NoteAdapter(Context context, List<NoteModel> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.filteredList = new ArrayList<>(noteList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        if (position < filteredList.size()) {
            NoteModel note = filteredList.get(position);
            holder.tvTitle.setText(note.getTitle());
            holder.tvContent.setText(note.getContent());
            holder.tvDate.setText(note.getDate());

            // Normal Tıklama
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(note, position);
                }
            });

            // BASILI TUTMA (LONG CLICK) - SİLME İÇİN
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(note, position);
                }
                return true; // Tıklama olayını tüketir
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateList(List<NoteModel> newList) {
        this.noteList = newList;
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.trim().isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (NoteModel item : noteList) {
                if ((item.getTitle() != null && item.getTitle().toLowerCase().contains(filterPattern)) ||
                        (item.getContent() != null && item.getContent().toLowerCase().contains(filterPattern))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
        }
    }
}