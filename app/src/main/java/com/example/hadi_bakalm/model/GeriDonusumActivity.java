package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.data.not_app_database;
import com.example.hadi_bakalm.data.notdao;
import com.example.hadi_bakalm.data.notentity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GeriDonusumActivity extends AppCompatActivity {

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR"));

    private RecyclerView rvTrashNotes;
    private LinearLayout layoutEmptyTrash;
    private EditText etSearchTrash;
    private TextView btnEmptyTrash;
    private ImageButton btnBackFromTrash;

    private notdao noteDao;
    private TrashAdapter adapter;
    private final List<notentity> trashList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geri_donusum);

        noteDao = not_app_database.getInstance(this).noteDao();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadTrashNotes();
    }

    private void initViews() {
        rvTrashNotes = findViewById(R.id.rvTrashNotes);
        layoutEmptyTrash = findViewById(R.id.layoutEmptyTrash);
        etSearchTrash = findViewById(R.id.etSearchTrash);
        btnEmptyTrash = findViewById(R.id.btnEmptyTrash);
        btnBackFromTrash = findViewById(R.id.btnBackFromTrash);
    }

    private void setupRecyclerView() {
        adapter = new TrashAdapter();
        rvTrashNotes.setLayoutManager(new LinearLayoutManager(this));
        rvTrashNotes.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnBackFromTrash != null) {
            btnBackFromTrash.setOnClickListener(v -> finish());
        }

        if (btnEmptyTrash != null) {
            btnEmptyTrash.setOnClickListener(v -> {
                if (trashList.isEmpty()) return;

                new AlertDialog.Builder(this)
                        .setTitle("Çöpü Boşalt")
                        .setMessage("Geri dönüşüm kutusundaki tüm notlar kalıcı olarak silinecektir. Bu işlem geri alınamaz.")
                        .setPositiveButton("Hepsini Sil", (dialog, which) -> emptyAllTrash())
                        .setNegativeButton("Vazgeç", null)
                        .show();
            });
        }

        if (etSearchTrash != null) {
            etSearchTrash.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterTrash(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadTrashNotes() {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> trashed = noteDao.getTrashNotes();
            runOnUiThread(() -> updateList(trashed));
        });
    }

    private void filterTrash(String query) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> allTrashed = noteDao.getTrashNotes();
            List<notentity> filtered = new ArrayList<>();

            for (notentity item : allTrashed) {
                if (item != null) {
                    boolean matchesTitle = item.title != null && item.title.toLowerCase().contains(query.toLowerCase());
                    boolean matchesContent = item.content != null && item.content.toLowerCase().contains(query.toLowerCase());
                    if (matchesTitle || matchesContent) {
                        filtered.add(item);
                    }
                }
            }

            runOnUiThread(() -> updateList(filtered));
        });
    }

    private void restoreNote(notentity note) {
        if (noteDao == null || note == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.restoreNoteFromTrash(note.id);
            runOnUiThread(() -> {
                Toast.makeText(this, "\"" + note.title + "\" geri yüklendi", Toast.LENGTH_SHORT).show();
                loadTrashNotes();
            });
        });
    }

    private void deletePermanently(notentity note) {
        if (noteDao == null || note == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.deleteNoteById(note.id);
            runOnUiThread(() -> {
                Toast.makeText(this, "Not kalıcı olarak silindi", Toast.LENGTH_SHORT).show();
                loadTrashNotes();
            });
        });
    }

    private void emptyAllTrash() {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.emptyTrash();
            runOnUiThread(() -> {
                Toast.makeText(this, "Geri dönüşüm kutusu temizlendi", Toast.LENGTH_SHORT).show();
                loadTrashNotes();
            });
        });
    }

    private void updateList(List<notentity> list) {
        trashList.clear();
        if (list != null) {
            trashList.addAll(list);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        boolean isEmpty = trashList.isEmpty();
        if (layoutEmptyTrash != null) {
            layoutEmptyTrash.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rvTrashNotes != null) {
            rvTrashNotes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (btnEmptyTrash != null) {
            btnEmptyTrash.setEnabled(!isEmpty);
            btnEmptyTrash.setAlpha(isEmpty ? 0.4f : 1.0f);
        }
    }

    private class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {

        @NonNull
        @Override
        public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trash_note_card, parent, false);
            return new TrashViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
            notentity note = trashList.get(position);
            if (note == null) return;

            holder.tvTrashTitle.setText(note.title != null && !note.title.trim().isEmpty() ? note.title : "Başlıksız Not");
            holder.tvTrashContent.setText(note.content != null && !note.content.trim().isEmpty() ? note.content : "İçerik önizlemesi yok");

            long sevenDaysMillis = TimeUnit.DAYS.toMillis(7);
            long timePassed = System.currentTimeMillis() - note.trashedTimestamp;
            long timeLeft = sevenDaysMillis - timePassed;
            long daysLeft = Math.max(1, TimeUnit.MILLISECONDS.toDays(timeLeft));

            holder.tvRemainingDays.setText(daysLeft + " gün kaldı");
            holder.tvDeletedDate.setText("Silindi: " + (note.trashedTimestamp > 0 ? DATE_FORMAT.format(new Date(note.trashedTimestamp)) : note.timestamp));

            holder.btnRestoreNote.setOnClickListener(v -> restoreNote(note));

            holder.btnDeletePermanently.setOnClickListener(v -> {
                new AlertDialog.Builder(GeriDonusumActivity.this)
                        .setTitle("Kalıcı Olarak Sil")
                        .setMessage("\"" + note.title + "\" notu tamamen silinsin mi?")
                        .setPositiveButton("Sil", (dialog, which) -> deletePermanently(note))
                        .setNegativeButton("Vazgeç", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return trashList.size();
        }

        class TrashViewHolder extends RecyclerView.ViewHolder {
            TextView tvTrashTitle, tvRemainingDays, tvTrashContent, tvDeletedDate, btnRestoreNote;
            ImageButton btnDeletePermanently;

            public TrashViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTrashTitle = itemView.findViewById(R.id.tvTrashTitle);
                tvRemainingDays = itemView.findViewById(R.id.tvRemainingDays);
                tvTrashContent = itemView.findViewById(R.id.tvTrashContent);
                tvDeletedDate = itemView.findViewById(R.id.tvDeletedDate);
                btnRestoreNote = itemView.findViewById(R.id.btnRestoreNote);
                btnDeletePermanently = itemView.findViewById(R.id.btnDeletePermanently);
            }
        }
    }
}