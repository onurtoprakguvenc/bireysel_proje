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
        void onLockClick(NoteModel note, int position);
    }

    private final List<NoteModel> noteList = new ArrayList<>();
    private final List<NoteModel> filteredList = new ArrayList<>();
    private boolean showPreviews = true;
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

    @SuppressLint("NotifyDataSetChanged")
    public void setShowPreviews(boolean showPreviews) {
        this.showPreviews = showPreviews;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<NoteModel> newList) {
        noteList.clear();
        filteredList.clear();
        if (newList != null) {
            noteList.addAll(newList);
            filteredList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = filteredList.get(position);
        if (note != null) {
            holder.bind(note, listener, showPreviews);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
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
        private final TextView tvCardEphemeralBadge;
        private final ImageButton btnQuickPin;
        private final ImageButton btnQuickDelete;
        private final ImageButton btnLockNote;
        private final FrameLayout layoutDrawingPreview;
        private final ImageView imgDrawingPreview;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvCardEphemeralBadge = itemView.findViewById(R.id.tvCardEphemeralBadge);
            btnLockNote = itemView.findViewById(R.id.btnLockNote);
            btnQuickPin = itemView.findViewById(R.id.btnPin);
            btnQuickDelete = itemView.findViewById(R.id.btnDeleteNote);
            layoutDrawingPreview = itemView.findViewById(R.id.layoutDrawingPreview);
            imgDrawingPreview = itemView.findViewById(R.id.imgDrawingPreview);
        }

        public void bind(NoteModel note, OnItemClickListener listener, boolean showPreviews) {
            if (note == null) return;

            tvNoteTitle.setText(note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle() : "Başlıksız Not");
            tvNoteDate.setText(note.getDate() != null ? note.getDate() : "");

            // 1. Kategori Rozeti
            String category = (note.getCategory() != null && !note.getCategory().trim().isEmpty()) ? note.getCategory() : "Genel";
            if (tvCategoryBadge != null) {
                String icon = getCategoryIcon(category);
                tvCategoryBadge.setText(icon.isEmpty() ? category : icon + " " + category);
            }

            // 2. KİLİTLİ NOT VEYA KOMPAKT MOD (ÖNİZLEMELERİ GİZLE) KONTROLÜ
            if (note.isLocked()) {
                if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                if (imgDrawingPreview != null) imgDrawingPreview.setVisibility(View.GONE);
                if (tvNoteContent != null) {
                    tvNoteContent.setVisibility(View.VISIBLE);
                    tvNoteContent.setText("Bu not kilitlidir. Görmek için dokunun.");
                    tvNoteContent.setTextColor(Color.parseColor("#94A3B8"));
                }
                if (btnLockNote != null) {
                    btnLockNote.setColorFilter(Color.parseColor("#0284C7"));
                }
            } else if (!showPreviews) {
                // KOMPAKT MOD: Önizlemeler kapalıysa çizim ve metin gizlenir
                if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                if (imgDrawingPreview != null) imgDrawingPreview.setVisibility(View.GONE);
                if (tvNoteContent != null) tvNoteContent.setVisibility(View.GONE);
                if (btnLockNote != null) {
                    btnLockNote.setColorFilter(Color.parseColor("#94A3B8"));
                }
            } else {
                // NORMAL MOD: Çizim veya metin önizlemesi
                if (btnLockNote != null) {
                    btnLockNote.setColorFilter(Color.parseColor("#94A3B8"));
                }

                String content = note.getContent() != null ? note.getContent().trim() : "";
                boolean hasBase64Image = content.startsWith("DRAWING_BASE64:") && content.length() > 20;

                if (hasBase64Image) {
                    if (tvNoteContent != null) tvNoteContent.setVisibility(View.GONE);

                    if (imgDrawingPreview != null) {
                        imgDrawingPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        try {
                            String cleanBase64 = content.replace("DRAWING_BASE64:", "").trim();
                            byte[] decodedString = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            if (decodedByte != null) {
                                imgDrawingPreview.setImageBitmap(decodedByte);
                                imgDrawingPreview.setVisibility(View.VISIBLE);
                                if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.VISIBLE);
                            } else {
                                if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (layoutDrawingPreview != null) layoutDrawingPreview.setVisibility(View.GONE);
                    if (tvNoteContent != null) {
                        String displayText = ("Çizim Notu".equals(content) || content.isEmpty()) ? "" : content;
                        if (displayText.isEmpty()) {
                            tvNoteContent.setVisibility(View.GONE);
                        } else {
                            tvNoteContent.setVisibility(View.VISIBLE);
                            tvNoteContent.setText(displayText);
                            tvNoteContent.setTextColor(Color.parseColor("#64748B"));
                        }
                    }
                }
            }

            // 3. Geçici Not Rozeti
            if (tvCardEphemeralBadge != null) {
                if (note.isEphemeral() && note.getExpireTimestamp() > 0) {
                    long diff = note.getExpireTimestamp() - System.currentTimeMillis();
                    if (diff <= 0) {
                        tvCardEphemeralBadge.setText("Süresi Doldu");
                        tvCardEphemeralBadge.setVisibility(View.VISIBLE);
                    } else {
                        long totalSeconds = diff / 1000;
                        long days = totalSeconds / (24 * 3600);
                        long hours = (totalSeconds % (24 * 3600)) / 3600;
                        long minutes = (totalSeconds % 3600) / 60;

                        String badgeText;
                        if (days > 0) {
                            badgeText = days + "g " + hours + "s";
                        } else if (hours > 0) {
                            badgeText = hours + "s " + minutes + "d";
                        } else if (minutes > 0) {
                            badgeText = minutes + " dk";
                        } else {
                            badgeText = "<1 dk";
                        }
                        tvCardEphemeralBadge.setText(badgeText);
                        tvCardEphemeralBadge.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvCardEphemeralBadge.setVisibility(View.GONE);
                }
            }

            // 4. Sabitleme İğnesi
            if (btnQuickPin != null) {
                btnQuickPin.setColorFilter(note.isPinned() ? Color.parseColor("#EAB308") : Color.parseColor("#94A3B8"));
                btnQuickPin.setOnClickListener(v -> {
                    if (listener != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onPinClick(note, pos);
                        }
                    }
                });
            }

            // 5. Kilit Butonu
            if (btnLockNote != null) {
                btnLockNote.setOnClickListener(v -> {
                    if (listener != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onLockClick(note, pos);
                        }
                    }
                });
            }

            // 6. Silme Butonu
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

            // 7. Karta Tıklama
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
                case "kişisel": return "";
                case "çizim": return "";
                case "geçici": return "";
                case "fikir & taslak": return "";
                default: return "";
            }
        }
    }
}