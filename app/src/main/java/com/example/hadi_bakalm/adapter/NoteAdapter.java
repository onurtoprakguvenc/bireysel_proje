package com.example.hadi_bakalm.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
        void onPinClick(NoteModel note, int position);
        void onDeleteClick(NoteModel note, int position);
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
        private final TextView tvCategoryBadge;
        private final ImageButton btnQuickPin;
        private final ImageButton btnQuickDelete;
        private final FrameLayout layoutDrawingPreview;
        private final ImageView imgDrawingPreview;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);

            btnQuickPin = itemView.findViewById(R.id.btnPin);
            btnQuickDelete = itemView.findViewById(R.id.btnDeleteNote);

            layoutDrawingPreview = itemView.findViewById(R.id.layoutDrawingPreview);
            imgDrawingPreview = itemView.findViewById(R.id.imgDrawingPreview);
        }

        public void bind(NoteModel note, OnItemClickListener listener) {
            if (note == null) return;

            tvNoteTitle.setText(note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle() : "Başlıksız Not");
            tvNoteDate.setText(note.getDate() != null ? note.getDate() : "");

            // 1. Kategori Rozeti (Badge)
            String category = (note.getCategory() != null && !note.getCategory().trim().isEmpty()) ? note.getCategory() : "Genel";
            if (tvCategoryBadge != null) {
                tvCategoryBadge.setText(getCategoryIcon(category) + " " + category);
            }

            // 2. Çizim ve İçerik Kontrolü
            String content = note.getContent() != null ? note.getContent().trim() : "";
            boolean hasBase64Image = content.startsWith("DRAWING_BASE64:") && content.length() > 20;

            if (hasBase64Image) {
                // Görseli çöz ve sarı kutuyu aç
                if (tvNoteContent != null) tvNoteContent.setVisibility(View.GONE);

                if (imgDrawingPreview != null) {
                    imgDrawingPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    try {
                        String cleanBase64 = content.replace("DRAWING_BASE64:", "").trim();
                        byte[] decodedString = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        if (decodedByte != null) {
                            imgDrawingPreview.setImageBitmap(decodedByte);
                            if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.VISIBLE);
                        } else {
                            if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                    }
                }
            } else {
                // Çizim görseli yoksa (boşsa ya da normal metin notuysa) sarı kutuyu tamamen kapat
                if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                if (tvNoteContent != null) {
                    tvNoteContent.setVisibility(View.VISIBLE);
                    String displayText = "Çizim Notu".equals(content) ? "" : content;
                    tvNoteContent.setText(displayText);
                }
            }

            // 3. Sabitleme İğne Durumu
            if (btnQuickPin != null) {
                if (note.isPinned()) {
                    btnQuickPin.setColorFilter(Color.parseColor("#EAB308"));
                } else {
                    btnQuickPin.setColorFilter(Color.parseColor("#94A3B8"));
                }

                btnQuickPin.setOnClickListener(v -> {
                    if (listener != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onPinClick(note, pos);
                        }
                    }
                });
            }

            // 4. Silme Butonu
            if (btnQuickDelete != null) {
                btnQuickDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(note, pos);
                        }
                    }
                });
            }

            // 5. Karta Tıklama
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(note, pos);
                    }
                }
            });
        }

        private String getCategoryIcon(String category) {
            switch (category.toLowerCase(Locale.getDefault())) {
                case "kişisel":
                    return "🏷️";
                case "çizim":
                    return "🎨";
                case "geçici":
                    return "⏱️";
                case "iş":
                case "iş / okul":
                    return "💼";
                default:
                    return "📌";
            }
        }
    }
}