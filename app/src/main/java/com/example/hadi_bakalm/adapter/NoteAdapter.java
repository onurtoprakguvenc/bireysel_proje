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

    private Context context;
    private List<NoteModel> noteList;
    private List<NoteModel> filteredList; // Arama/filtreleme için kopya liste

    public NoteAdapter(Context context, List<NoteModel> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.filteredList = new ArrayList<>(noteList);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = filteredList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        holder.tvDate.setText(note.getDate());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // Arama Çubuğu Filtreleme Metodu
    public void filter(String query) {
        filteredList.clear();
        if (query.trim().isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (NoteModel item : noteList) {
                if (item.getTitle().toLowerCase().contains(filterPattern) ||
                        item.getContent().toLowerCase().contains(filterPattern)) {
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