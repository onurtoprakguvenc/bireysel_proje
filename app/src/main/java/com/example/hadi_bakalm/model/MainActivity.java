package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.NoteAdapter;
import com.example.hadi_bakalm.data.NoteCleanupWorker;
import com.example.hadi_bakalm.data.not_app_database;
import com.example.hadi_bakalm.data.notdao;
import com.example.hadi_bakalm.data.notentity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Locale TR_LOCALE = new Locale("tr", "TR");

    // Arayüz Elemanları
    private RecyclerView rvNotes;
    private EditText etSearch;
    private ImageButton btnLibraryBridge;
    private ImageButton btnToggleLayout;
    private FloatingActionButton fabAddNote;
    private ImageButton fabDonateCoffee;
    private TextView tvNoteCount;
    private LinearLayout categoryChipContainer;

    // Adaptör ve Veri Yönetimi
    private NoteAdapter noteAdapter;
    private final List<NoteModel> noteList = new ArrayList<>();
    private boolean isGridMode = false;

    // Room Veritabanı
    private notdao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asil_ana_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        setupPeriodicCleanupWorker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (noteDao != null) {
            DB_EXECUTOR.execute(() -> {
                noteDao.moveExpiredNotesToTrash(System.currentTimeMillis());
                loadNotesFromDatabase();
                loadDynamicCategoryChips();
            });
        } else {
            loadNotesFromDatabase();
            loadDynamicCategoryChips();
        }
    }

    private void setupPeriodicCleanupWorker() {
        PeriodicWorkRequest cleanupRequest = new PeriodicWorkRequest.Builder(
                NoteCleanupWorker.class,
                1, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NoteAutoCleanupWork",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
        );
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rvNotes);
        etSearch = findViewById(R.id.etSearch);
        btnLibraryBridge = findViewById(R.id.btnLibraryBridge);
        btnToggleLayout = findViewById(R.id.btnToggleLayout);
        fabAddNote = findViewById(R.id.fabAddNote);
        fabDonateCoffee = findViewById(R.id.fabDonateCoffee);
        tvNoteCount = findViewById(R.id.tvNoteCount);
        categoryChipContainer = findViewById(R.id.categoryChipContainer);
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter(noteList);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NoteModel note, int position) {
                if (note == null) return;
                Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
                intent.putExtra("EXTRA_NOTE_ID", note.getId());
                intent.putExtra("EXTRA_NOTE_TITLE", note.getTitle());
                intent.putExtra("EXTRA_NOTE_CONTENT", note.getContent());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(NoteModel note, int position) {
                if (note == null) return;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Notu Sil")
                        .setMessage("\"" + note.getTitle() + "\" başlıklı notu silmek istediğinize emin misiniz?")
                        .setPositiveButton("Sil", (dialog, which) -> deleteNoteFromDatabase(note.getId()))
                        .setNegativeButton("İptal", null)
                        .show();
            }
        });

        if (rvNotes != null) {
            rvNotes.setLayoutManager(new LinearLayoutManager(this));
            rvNotes.setAdapter(noteAdapter);
        }
    }

    private void loadNotesFromDatabase() {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> dbEntities = noteDao.getAllNotes();
            List<NoteModel> updatedList = mapEntitiesToModels(dbEntities);

            runOnUiThread(() -> applyListUpdate(updatedList));
        });
    }

    private void deleteNoteFromDatabase(int noteId) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.deleteNoteById(noteId);

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Not silindi", Toast.LENGTH_SHORT).show();
                loadNotesFromDatabase();
                loadDynamicCategoryChips();
            });
        });
    }

    private void setupClickListeners() {
        if (btnLibraryBridge != null) {
            btnLibraryBridge.setOnClickListener(v -> openLibraryActivity());
        }

        if (btnToggleLayout != null) {
            btnToggleLayout.setOnClickListener(v -> {
                isGridMode = !isGridMode;
                if (rvNotes != null) {
                    if (isGridMode) {
                        rvNotes.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                        btnToggleLayout.setImageResource(R.drawable.ic_list);
                    } else {
                        rvNotes.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        btnToggleLayout.setImageResource(R.drawable.ic_grid);
                    }
                }
            });
        }

        if (fabAddNote != null) {
            fabAddNote.setOnClickListener(v -> showCustomNoteCreationDialog());
        }

        if (fabDonateCoffee != null) {
            fabDonateCoffee.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, not_bagis_sayfa.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    /**
     * not_ekleme.xml tasarımını tetikleyen özel diyalog
     */
    @SuppressLint("InflateParams")
    private void showCustomNoteCreationDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.not_ekleme, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etNewNoteTitle = dialogView.findViewById(R.id.etNewNoteTitle);
        ImageButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        TextView btnCancelDialog = dialogView.findViewById(R.id.btnCancelDialog);

        RelativeLayout cardCategoryPersonal = dialogView.findViewById(R.id.cardCategoryPersonal);
        RelativeLayout cardCategoryEphemeral = dialogView.findViewById(R.id.cardCategoryEphemeral);
        RelativeLayout cardCategoryWork = dialogView.findViewById(R.id.cardCategoryWork);
        TextView btnAddCustomCategory = dialogView.findViewById(R.id.btnAddCustomCategory);

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnCancelDialog != null) {
            btnCancelDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (cardCategoryPersonal != null) {
            cardCategoryPersonal.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                openNoteEditor(title, "Kişisel");
                dialog.dismiss();
            });
        }

        if (cardCategoryEphemeral != null) {
            cardCategoryEphemeral.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                openNoteEditor(title, "Geçici");
                dialog.dismiss();
            });
        }

        if (cardCategoryWork != null) {
            cardCategoryWork.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                openNoteEditor(title, "İş / Okul");
                dialog.dismiss();
            });
        }

        if (btnAddCustomCategory != null) {
            btnAddCustomCategory.setOnClickListener(v -> {
                dialog.dismiss();
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                showCustomCategoryInputDialog(title);
            });
        }

        // showCustomNoteCreationDialog içerisindeki cardCategoryEphemeral listener'ı:
        if (cardCategoryEphemeral != null) {
            cardCategoryEphemeral.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                dialog.dismiss();
                showEphemeralDurationDialog(title);
            });
        }

        dialog.show();
    }

    private void showCustomCategoryInputDialog(String prefilledTitle) {
        final EditText input = new EditText(this);
        input.setHint("Kategori adını girin (Örn: Proje, Alışveriş)");
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Yeni Kategori İsmi")
                .setView(input)
                .setPositiveButton("Oluştur ve Not Aç", (dialog, which) -> {
                    String customCategory = input.getText().toString().trim();
                    if (customCategory.isEmpty()) {
                        customCategory = "Genel";
                    }
                    openNoteEditor(prefilledTitle, customCategory);
                })
                .setNegativeButton("İptal", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void openNoteEditor(String title, String category) {
        Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
        if (!title.isEmpty()) {
            intent.putExtra("EXTRA_NOTE_TITLE", title);
        }
        intent.putExtra("EXTRA_NOTE_CATEGORY", category);
        startActivity(intent);
    }

    private void setupSearchListener() {
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = (s != null) ? s.toString() : "";
                filterNotesFromDatabase(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotesFromDatabase(String query) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> dbEntities;
            if (query.trim().isEmpty()) {
                dbEntities = noteDao.getAllNotes();
            } else {
                dbEntities = noteDao.searchNotes(query);
            }

            List<NoteModel> filteredList = mapEntitiesToModels(dbEntities);
            runOnUiThread(() -> applyListUpdate(filteredList));
        });
    }

    private void loadDynamicCategoryChips() {
        if (categoryChipContainer == null || noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> allNotes = noteDao.getAllNotes();
            List<String> dynamicCategories = extractDynamicCategories(allNotes);

            runOnUiThread(() -> {
                categoryChipContainer.removeAllViews();

                for (String categoryName : dynamicCategories) {
                    TextView chip = new TextView(this);
                    chip.setText(categoryName);
                    chip.setTextSize(12f);
                    chip.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, dpToPx(8), 0);
                    chip.setLayoutParams(params);

                    if (categoryName.equalsIgnoreCase("Tümü")) {
                        chip.setBackgroundResource(R.drawable.bg_chip_active);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                    } else {
                        chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                        chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                    }

                    chip.setOnClickListener(v -> {
                        resetChipStyles();
                        chip.setBackgroundResource(R.drawable.bg_chip_active);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                        if (categoryName.equalsIgnoreCase("Tümü")) {
                            loadNotesFromDatabase();
                        } else {
                            filterNotesByCategory(categoryName);
                        }
                    });

                    categoryChipContainer.addView(chip);
                }
            });
        });
    }

    private List<String> extractDynamicCategories(List<notentity> allNotes) {
        List<String> dynamicCategories = new ArrayList<>();
        dynamicCategories.add("Tümü");
        dynamicCategories.add("Kişisel");
        dynamicCategories.add("Geçici");

        if (allNotes != null) {
            for (notentity note : allNotes) {
                if (note != null && note.category != null && !note.category.trim().isEmpty()) {
                    String cat = note.category.trim();
                    if (!dynamicCategories.contains(cat)) {
                        dynamicCategories.add(cat);
                    }
                }
            }
        }
        return dynamicCategories;
    }

    private void filterNotesByCategory(String category) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> dbEntities = noteDao.getNotesByCategory(category);
            List<NoteModel> filteredList = mapEntitiesToModels(dbEntities);

            runOnUiThread(() -> applyListUpdate(filteredList));
        });
    }

    private List<NoteModel> mapEntitiesToModels(List<notentity> dbEntities) {
        List<NoteModel> models = new ArrayList<>();
        if (dbEntities != null) {
            for (notentity entity : dbEntities) {
                if (entity != null) {
                    models.add(new NoteModel(
                            entity.id,
                            entity.title,
                            entity.content,
                            entity.timestamp,
                            entity.category,
                            entity.isPinned
                    ));
                }
            }
        }
        return models;
    }

    private void applyListUpdate(List<NoteModel> newList) {
        noteList.clear();
        noteList.addAll(newList);
        if (noteAdapter != null) {
            noteAdapter.updateList(noteList);
        }
        updateNoteCount(noteList.size());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void resetChipStyles() {
        for (int i = 0; i < categoryChipContainer.getChildCount(); i++) {
            View child = categoryChipContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    private void openLibraryActivity() {
        Intent intent = new Intent(MainActivity.this, EskiMainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateNoteCount(int count) {
        if (tvNoteCount != null) {
            tvNoteCount.setText(String.format(TR_LOCALE, "Toplam %d kayıtlı not", count));
        }
    }

    private long selectedEphemeralTimestamp = 0L;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void showEphemeralDurationDialog(String noteTitle) {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gecici_sure_secimi, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtSelectedExpiryInfo = dialogView.findViewById(R.id.txtSelectedExpiryInfo);
        TextView btnQuick1Hour = dialogView.findViewById(R.id.btnQuick1Hour);
        TextView btnQuick24Hours = dialogView.findViewById(R.id.btnQuick24Hours);
        TextView btnQuick3Days = dialogView.findViewById(R.id.btnQuick3Days);
        TextView btnCustomDateTime = dialogView.findViewById(R.id.btnCustomDateTime);

        TextView btnCancelExpiry = dialogView.findViewById(R.id.btnCancelExpiry);
        TextView btnConfirmExpiry = dialogView.findViewById(R.id.btnConfirmExpiry);

        // Varsayılan: 1 Saat Sonrası
        selectedEphemeralTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);

        btnQuick1Hour.setOnClickListener(v -> {
            selectedEphemeralTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);
            setDurationTabActive(btnQuick1Hour, btnQuick24Hours, btnQuick3Days);
            txtSelectedExpiryInfo.setText("Süre: 1 Saat sonra geri dönüşüme taşınacak");
        });

        btnQuick24Hours.setOnClickListener(v -> {
            selectedEphemeralTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
            setDurationTabActive(btnQuick24Hours, btnQuick1Hour, btnQuick3Days);
            txtSelectedExpiryInfo.setText("Süre: 24 Saat sonra geri dönüşüme taşınacak");
        });

        btnQuick3Days.setOnClickListener(v -> {
            selectedEphemeralTimestamp = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L);
            setDurationTabActive(btnQuick3Days, btnQuick1Hour, btnQuick24Hours);
            txtSelectedExpiryInfo.setText("Süre: 3 Gün sonra geri dönüşüme taşınacak");
        });

        btnCustomDateTime.setOnClickListener(v -> {
            Calendar takvim = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                takvim.set(Calendar.YEAR, year);
                takvim.set(Calendar.MONTH, month);
                takvim.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePicker = new TimePickerDialog(this, (tView, hourOfDay, minute) -> {
                    takvim.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    takvim.set(Calendar.MINUTE, minute);
                    takvim.set(Calendar.SECOND, 0);

                    long chosen = takvim.getTimeInMillis();
                    if (chosen > System.currentTimeMillis()) {
                        selectedEphemeralTimestamp = chosen;
                        resetDurationTabs(btnQuick1Hour, btnQuick24Hours, btnQuick3Days);
                        txtSelectedExpiryInfo.setText("Bitiş: " + dayOfMonth + "/" + (month + 1) + "/" + year + " " + String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    } else {
                        Toast.makeText(this, "Geçmiş bir zaman seçemezsiniz!", Toast.LENGTH_SHORT).show();
                    }
                }, takvim.get(Calendar.HOUR_OF_DAY), takvim.get(Calendar.MINUTE), true);
                timePicker.show();
            }, takvim.get(Calendar.YEAR), takvim.get(Calendar.MONTH), takvim.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnCancelExpiry.setOnClickListener(v -> dialog.dismiss());

        btnConfirmExpiry.setOnClickListener(v -> {
            dialog.dismiss();
            openEphemeralNoteEditor(noteTitle, selectedEphemeralTimestamp);
        });

        dialog.show();
    }

    private void setDurationTabActive(TextView active, TextView... inactives) {
        active.setBackgroundResource(R.drawable.bg_black_pill);
        active.setTextColor(Color.WHITE);
        for (TextView in : inactives) {
            in.setBackgroundResource(R.drawable.bg_chip_inactive);
            in.setTextColor(Color.parseColor("#475569"));
        }
    }

    private void resetDurationTabs(TextView... tabs) {
        for (TextView t : tabs) {
            t.setBackgroundResource(R.drawable.bg_chip_inactive);
            t.setTextColor(Color.parseColor("#475569"));
        }
    }

    private void openEphemeralNoteEditor(String title, long expireTimestamp) {
        Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
        if (!title.isEmpty()) {
            intent.putExtra("EXTRA_NOTE_TITLE", title);
        }
        intent.putExtra("EXTRA_NOTE_CATEGORY", "Geçici");
        intent.putExtra("EXTRA_IS_EPHEMERAL", true);
        intent.putExtra("EXTRA_EXPIRE_TIMESTAMP", expireTimestamp);
        startActivity(intent);
    }
}