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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Arayüz Elemanları
    private RecyclerView rvNotes;
    private EditText etSearch;
    private ImageButton btnLibraryBridge, btnToggleLayout;
    private FloatingActionButton fabAddNote, fabDonateCoffee;
    private BottomNavigationView bottomNavigation;
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

        // Room Veritabanı DAO Başlatma
        noteDao = not_app_database.getInstance(this).noteDao();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        setupCategoryChips();
        setupBottomNavigation();
    }

    // SAYFA HER ÖN PLANA GELDİĞİNDE VERİTABANINDAN CANLI VERİ ÇEKER
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
        bottomNavigation = findViewById(R.id.bottomNavigation);
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

            // KARTA BASILI TUTULDUĞUNDA ÇALIŞIR (SİLME İŞLEMİ)
            @Override
            public void onItemLongClick(NoteModel note, int position) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
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

    // VERİTABANINDAN TÜM NOTLARI ID DAHİL ÇEKEN METOD
    private void loadNotesFromDatabase() {
        if (noteDao == null) return;

        List<notentity> dbEntities = noteDao.getAllNotes();
        noteList.clear();

        for (notentity entity : dbEntities) {
            noteList.add(new NoteModel(
                    entity.id,
                    entity.title,
                    entity.content,
                    entity.timestamp,
                    entity.category,
                    entity.isPinned
            ));
        }

        if (noteAdapter != null) {
            noteAdapter.updateList(noteList);
        }
        updateNoteCount(noteList.size());
    }

    private void deleteNoteFromDatabase(int noteId) {
        if (noteDao == null) return;

        notentity noteToDelete = new notentity("", "", "", "", "");
        noteToDelete.id = noteId;

        noteDao.deleteNote(noteToDelete);
        Toast.makeText(this, "Not silindi", Toast.LENGTH_SHORT).show();

        loadNotesFromDatabase();
    }

    private void setupClickListeners() {
        btnLibraryBridge.setOnClickListener(v -> openLibraryActivity());

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

        fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
            startActivity(intent);
        });

        if (fabDonateCoffee != null) {
            fabDonateCoffee.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, not_bagis_sayfa.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void setupSearchListener() {
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

        List<notentity> dbEntities;
        if (query.trim().isEmpty()) {
            dbEntities = noteDao.getAllNotes();
        } else {
            dbEntities = noteDao.searchNotes(query);
        }

        noteList.clear();
        for (notentity entity : dbEntities) {
            noteList.add(new NoteModel(
                    entity.id,
                    entity.title,
                    entity.content,
                    entity.timestamp,
                    entity.category,
                    entity.isPinned
            ));
        }

        if (noteAdapter != null) {
            noteAdapter.updateList(noteList);
        }
        updateNoteCount(noteList.size());
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

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_notes) {
                return true;
            } else if (id == R.id.nav_library) {
                openLibraryActivity();
                return true;
            } else if (id == R.id.nav_settings) {
                Toast.makeText(MainActivity.this, "Ayarlar Açılıyor...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
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