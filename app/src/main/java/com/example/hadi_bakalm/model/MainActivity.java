package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.NoteAdapter;
import com.example.hadi_bakalm.data.not_app_database;
import com.example.hadi_bakalm.data.notdao;
import com.example.hadi_bakalm.data.notentity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // Arayüz Elemanları
    private RecyclerView rvNotes;
    private EditText etSearch;
    private ImageButton btnLibraryBridge, btnToggleLayout;
    private FloatingActionButton fabAddNote, fabDonateCoffee;
    private TextView tvNoteCount;
    private LinearLayout categoryChipContainer;

    // Adaptör ve Veri Yönetimi
    private NoteAdapter noteAdapter;
    private List<NoteModel> noteList;
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
        setupCategoryChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromDatabase();
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
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(this, noteList);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NoteModel note, int position) {
                Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
                intent.putExtra("EXTRA_NOTE_ID", note.getId());
                intent.putExtra("EXTRA_NOTE_TITLE", note.getTitle());
                intent.putExtra("EXTRA_NOTE_CONTENT", note.getContent());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(NoteModel note, int position) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Notu Sil")
                        .setMessage("\"" + note.getTitle() + "\" başlıklı notu silmek istediğinize emin misiniz?")
                        .setPositiveButton("Sil", (dialog, which) -> {
                            deleteNoteFromDatabase(note.getId());
                        })
                        .setNegativeButton("İptal", null)
                        .show();
            }
        });

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(noteAdapter);
    }

    // VERİTABANINDAN VERİLERİ ARKA PLANDA ÇEKER
    private void loadNotesFromDatabase() {
        if (noteDao == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<notentity> dbEntities = noteDao.getAllNotes();
            List<NoteModel> updatedList = new ArrayList<>();

            for (notentity entity : dbEntities) {
                updatedList.add(new NoteModel(
                        entity.id,
                        entity.title,
                        entity.content,
                        entity.timestamp,
                        entity.category,
                        entity.isPinned
                ));
            }

            runOnUiThread(() -> {
                noteList.clear();
                noteList.addAll(updatedList);
                if (noteAdapter != null) {
                    noteAdapter.updateList(noteList);
                }
                updateNoteCount(noteList.size());
            });
        });
    }

    private void deleteNoteFromDatabase(int noteId) {
        if (noteDao == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            notentity noteToDelete = new notentity("", "", "", "", "");
            noteToDelete.id = noteId;
            noteDao.deleteNote(noteToDelete);

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Not silindi", Toast.LENGTH_SHORT).show();
                loadNotesFromDatabase();
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
                if (isGridMode) {
                    rvNotes.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                    btnToggleLayout.setImageResource(R.drawable.ic_list);
                } else {
                    rvNotes.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    btnToggleLayout.setImageResource(R.drawable.ic_grid);
                }
            });
        }

        if (fabAddNote != null) {
            fabAddNote.setOnClickListener(v -> showCategorySelectionDialog());
        }

        if (fabDonateCoffee != null) {
            fabDonateCoffee.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, not_bagis_sayfa.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void showCategorySelectionDialog() {
        String[] categories = {"Kişisel", "Geçici", "İş / Okul", "+ Yeni Kategori Ekle"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hangi kategori olsun?");
        builder.setItems(categories, (dialog, which) -> {
            if (which == categories.length - 1) {
                showCustomCategoryInputDialog();
            } else {
                String selectedCategory = categories[which];
                openNoteEditorWithCategory(selectedCategory);
            }
        });

        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    private void showCustomCategoryInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yeni Kategori İsmi");

        final EditText input = new EditText(this);
        input.setHint("Kategori adını girin (Örn: Proje, Alışveriş)");
        input.setPadding(48, 32, 48, 32);
        builder.setView(input);

        builder.setPositiveButton("Oluştur ve Not Aç", (dialog, which) -> {
            String customCategory = input.getText().toString().trim();
            if (customCategory.isEmpty()) {
                customCategory = "Genel";
            }
            openNoteEditorWithCategory(customCategory);
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openNoteEditorWithCategory(String category) {
        Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
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
                filterNotesFromDatabase(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotesFromDatabase(String query) {
        if (noteDao == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<notentity> dbEntities;
            if (query.trim().isEmpty()) {
                dbEntities = noteDao.getAllNotes();
            } else {
                dbEntities = noteDao.searchNotes(query);
            }

            List<NoteModel> filteredList = new ArrayList<>();
            for (notentity entity : dbEntities) {
                filteredList.add(new NoteModel(
                        entity.id,
                        entity.title,
                        entity.content,
                        entity.timestamp,
                        entity.category,
                        entity.isPinned
                ));
            }

            runOnUiThread(() -> {
                noteList.clear();
                noteList.addAll(filteredList);
                if (noteAdapter != null) {
                    noteAdapter.updateList(noteList);
                }
                updateNoteCount(noteList.size());
            });
        });
    }

    private void setupCategoryChips() {
        if (categoryChipContainer == null) return;

        for (int i = 0; i < categoryChipContainer.getChildCount(); i++) {
            View child = categoryChipContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setOnClickListener(v -> {
                    resetChipStyles();

                    chip.setBackgroundResource(R.drawable.bg_chip_active);
                    chip.setTextColor(getResources().getColor(android.R.color.white));

                    String categoryText = chip.getText().toString();
                    if (categoryText.equalsIgnoreCase("Tümü") || categoryText.equalsIgnoreCase("All")) {
                        loadNotesFromDatabase();
                    } else {
                        filterNotesFromDatabase(categoryText);
                    }
                });
            }
        }
    }

    private void resetChipStyles() {
        for (int i = 0; i < categoryChipContainer.getChildCount(); i++) {
            View child = categoryChipContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                chip.setTextColor(getResources().getColor(R.color.text_secondary));
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
            tvNoteCount.setText("Toplam " + count + " kayıtlı not");
        }
    }
}