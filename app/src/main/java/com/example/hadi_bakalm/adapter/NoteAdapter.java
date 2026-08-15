package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NoteModel note, int position);
        void onItemLongClick(NoteModel note, int position);
    }

    private final Context context;
    private List<NoteModel> noteList;
    private List<NoteModel> filteredList;
    private OnItemClickListener listener;

    public NoteAdapter(Context context, List<NoteModel> noteList) {
        this.context = context;
        this.noteList = noteList != null ? noteList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.noteList);
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

            holder.tvNoteTitle.setText(note.getTitle());
            holder.tvNoteContent.setText(note.getContent());
            holder.tvNoteDate.setText(note.getDate());

            if (note.isPinned()) {
                holder.ivPinBadge.setVisibility(View.VISIBLE);
            } else {
                holder.ivPinBadge.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(note, pos);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(note, pos);
                    }
                }
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<NoteModel> newList) {
        this.noteList = newList != null ? newList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.noteList);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unused")
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
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
        TextView tvNoteTitle, tvNoteContent, tvNoteDate;
        ImageView ivPinBadge;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            ivPinBadge = itemView.findViewById(R.id.ivPinBadge);
        }
    }
}