package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.NoteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NoteModel note, int position);
        void onItemLongClick(NoteModel note, int position);
    }

    private final List<NoteModel> noteList = new ArrayList<>();
    private final List<NoteModel> filteredList = new ArrayList<>();
    private OnItemClickListener listener;

    public NoteAdapter(List<NoteModel> initialList) {
        if (initialList != null) {
            this.noteList.addAll(initialList);
            this.filteredList.addAll(initialList);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(filteredList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<NoteModel> newList) {
        this.noteList.clear();
        this.filteredList.clear();
        if (newList != null) {
            this.noteList.addAll(newList);
            this.filteredList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unused")
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            String filterPattern = query.toLowerCase(Locale.getDefault()).trim();
            for (NoteModel item : noteList) {
                if (item != null) {
                    boolean matchesTitle = item.getTitle() != null && item.getTitle().toLowerCase(Locale.getDefault()).contains(filterPattern);
                    boolean matchesContent = item.getContent() != null && item.getContent().toLowerCase(Locale.getDefault()).contains(filterPattern);
                    if (matchesTitle || matchesContent) {
                        filteredList.add(item);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNoteTitle;
        private final TextView tvNoteContent;
        private final TextView tvNoteDate;
        private final ImageView ivPinBadge;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            ivPinBadge = itemView.findViewById(R.id.ivPinBadge);
        }

        public void bind(NoteModel note, OnItemClickListener listener) {
            if (note == null) return;

            tvNoteTitle.setText(note.getTitle() != null ? note.getTitle() : "");
            tvNoteContent.setText(note.getContent() != null ? note.getContent() : "");
            tvNoteDate.setText(note.getDate() != null ? note.getDate() : "");

            ivPinBadge.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(note, pos);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(note, pos);
                    }
                }
                return true;
            });
        }
    }
}