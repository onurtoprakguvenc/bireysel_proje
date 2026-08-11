package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.NoteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Arayüz Elemanları
    private RecyclerView rvNotes;
    private EditText etSearch;
    private ImageButton btnLibraryBridge, btnToggleLayout;
    private FloatingActionButton fabAddNote;
    private BottomNavigationView bottomNavigation;
    private TextView tvNoteCount;

    // Adaptör ve Düzen Yönetimi
    private NoteAdapter noteAdapter;
    private List<NoteModel> noteList;
    private boolean isGridMode = false; // Grid / Liste Görünümü Durumu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asil_ana_sayfa);

        // Arayüz Elemanlarını Bağlama
        initViews();

        // Veri Listesi ve Adaptör Kurulumu
        setupRecyclerView();

        // Dinleyicileri (Listeners) Başlatma
        setupClickListeners();
        setupSearchListener();
        setupBottomNavigation();
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rvNotes);
        etSearch = findViewById(R.id.etSearch);
        btnLibraryBridge = findViewById(R.id.btnLibraryBridge);
        btnToggleLayout = findViewById(R.id.btnToggleLayout);
        fabAddNote = findViewById(R.id.fabAddNote);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvNoteCount = findViewById(R.id.tvNoteCount);
    }

    private void setupRecyclerView() {
        noteList = new ArrayList<>();
        // Test Verileri (Daha sonra Room Veritabanı bağlanacak)
        noteList.add(new NoteModel("Haftalık Kitap Notları", "Mikro alışkanlıklar üzerine odaklan.", "Dün, 14:20", "Fikirler", true));
        noteList.add(new NoteModel("Mobil Arayüz Tasarımı", "Minimalist not uygulaması tasarla.", "Bugün, 09:15", "İş", false));
        noteList.add(new NoteModel("Alışveriş Listesi", "Filtre kahve, yulaf ezmesi...", "08 Ağu", "Kişisel", false));

        noteAdapter = new NoteAdapter(this, noteList);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(noteAdapter);

        updateNoteCount();
    }

    private void setupClickListeners() {
        // İŞTE O KRİTİK KÖPRÜ: Zihinsel Kütüphane / Kavramlar Ekranına Geçiş
        btnLibraryBridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLibraryActivity();
            }
        });

        // Grid / Liste Görünümü Değiştirme Butonu
        btnToggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGridMode = !isGridMode;
                if (isGridMode) {
                    rvNotes.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                    btnToggleLayout.setImageResource(R.drawable.ic_list); // Liste ikonuna çevir
                } else {
                    rvNotes.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    btnToggleLayout.setImageResource(R.drawable.ic_grid); // Grid ikonuna çevir
                }
            }
        });

        // Yeni Not Ekleme FAB -> Editör Ekranı (not_alma_sayfa) Bağlandı
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
                startActivity(intent);
            }
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (noteAdapter != null) {
                    noteAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_notes) {
                    // Zaten buradayız
                    return true;
                } else if (id == R.id.nav_library) {
                    // ALT BARDAN KÜTÜPHANEYE GEÇİŞ KÖPRÜSÜ
                    openLibraryActivity();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(MainActivity.this, "Ayarlar Açılıyor...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    // KÖPRÜ METODU: Kavramlar ve Kişisel Metinlerin Olduğu Aktiviteyi Çağırır
    private void openLibraryActivity() {
        Intent intent = new Intent(MainActivity.this, EskiMainActivity.class);
        startActivity(intent);
        // Yumuşak geçiş animasyonu (isteğe bağlı)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateNoteCount() {
        if (tvNoteCount != null && noteList != null) {
            tvNoteCount.setText("Toplam " + noteList.size() + " kayıtlı not");
        }
    }
}