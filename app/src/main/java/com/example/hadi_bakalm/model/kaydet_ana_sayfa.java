package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.model.NavigationHelper;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;

import java.util.ArrayList;
import java.util.List;

public class kaydet_ana_sayfa extends AppCompatActivity {

    private RecyclerView recyclerViewSaved;
    private kaydedilenler_adapter adapter;
    private List<kaydedilenler> savedList;
    private List<kaydedilenler> currentFilteredList; // Aktif filtrelenmiş liste
    private TextView txtItemCount;
    private ImageView btnBack;
    private EditText etSearch;

    private TextView btnFilterAll, btnFilterConcepts, btnFilterTexts;
    private String selectedType = "ALL"; // "ALL", "KAVRAM", "METİN"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kaydet_sayfa_gorme);

        NavigationHelper.setupBottomNavigation(this);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        setupSearchAndFilters();
    }

    private void initViews() {
        recyclerViewSaved = findViewById(R.id.recyclerViewSaved);
        txtItemCount = findViewById(R.id.txtItemCount);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterConcepts = findViewById(R.id.btnFilterConcepts);
        btnFilterTexts = findViewById(R.id.btnFilterTexts);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(kaydet_ana_sayfa.this, com.example.hadi_bakalm.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> {
                selectedType = "ALL";
                updateFilterUI();
                applyFilterAndSearch();
            });
        }

        if (btnFilterConcepts != null) {
            btnFilterConcepts.setOnClickListener(v -> {
                selectedType = "KAVRAM";
                updateFilterUI();
                applyFilterAndSearch();
            });
        }

        if (btnFilterTexts != null) {
            btnFilterTexts.setOnClickListener(v -> {
                selectedType = "METİN";
                updateFilterUI();
                applyFilterAndSearch();
            });
        }
    }

    private void setupRecyclerView() {
        recyclerViewSaved.setLayoutManager(new LinearLayoutManager(this));

        // Test Verileri
        savedList = new ArrayList<>();
        savedList.add(new kaydedilenler("1", "başlık 1 – düşünce mimarisi", "Zihinsel modeller, dünyayı anlama ve karmaşık durumları basitleştirme şeklimizi belirler.", "METİN", "Kişisel Not", "Dün eklendi"));
        savedList.add(new kaydedilenler("2", "batık maliyet yanılsaması", "Geçmişte harcanan zaman veya para yüzünden zararlı bir karara devam etme eğilimi.", "KAVRAM", "Karar Teorisi", "3 gün önce eklendi"));
        savedList.add(new kaydedilenler("3", "başlık 3 – dijital sadeleşme", "Gürültüden uzaklaşmak ve zihinsel sakinlik sağlamak için sadeleşme rehberi.", "METİN", "Minimalizm", "1 ay önce eklendi"));

        currentFilteredList = new ArrayList<>(savedList);

        adapter = new kaydedilenler_adapter(this, currentFilteredList);
        recyclerViewSaved.setAdapter(adapter);

        updateCount(currentFilteredList.size());
    }

    private void setupSearchAndFilters() {
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilterAndSearch() {
        java.util.Locale trLocale = new java.util.Locale("tr", "TR");

        String query = etSearch != null ? etSearch.getText().toString().toLowerCase(trLocale).trim() : "";
        List<kaydedilenler> filtered = new ArrayList<>();

        for (kaydedilenler item : savedList) {
            boolean matchesType = selectedType.equals("ALL") || item.getType().equalsIgnoreCase(selectedType);

            // Türkçe Locale ile dönüştürerek arama yapıyoruz
            String baslik = item.getTitle() != null ? item.getTitle().toLowerCase(trLocale) : "";
            String aciklama = item.getDescription() != null ? item.getDescription().toLowerCase(trLocale) : "";

            boolean matchesQuery = baslik.contains(query) || aciklama.contains(query);

            if (matchesType && matchesQuery) {
                filtered.add(item);
            }
        }

        currentFilteredList = filtered;
        if (adapter != null) {
            adapter.filterList(filtered);
        }
        updateCount(filtered.size());
    }

    private void updateFilterUI() {
        // Buton arka planlarını seçilen tipe göre görsel olarak günceller
        if (btnFilterAll != null) {
            btnFilterAll.setBackgroundResource(selectedType.equals("ALL") ? R.drawable.bg_black_icon_box : R.drawable.bg_kaydet_button);
            btnFilterAll.setTextColor(selectedType.equals("ALL") ? 0xFFFFFFFF : 0xFF475569);
        }
        if (btnFilterConcepts != null) {
            btnFilterConcepts.setBackgroundResource(selectedType.equals("KAVRAM") ? R.drawable.bg_black_icon_box : R.drawable.bg_kaydet_button);
            btnFilterConcepts.setTextColor(selectedType.equals("KAVRAM") ? 0xFFFFFFFF : 0xFF475569);
        }
        if (btnFilterTexts != null) {
            btnFilterTexts.setBackgroundResource(selectedType.equals("METİN") ? R.drawable.bg_black_icon_box : R.drawable.bg_kaydet_button);
            btnFilterTexts.setTextColor(selectedType.equals("METİN") ? 0xFFFFFFFF : 0xFF475569);
        }
    }

    private void updateCount(int count) {
        if (txtItemCount != null) {
            txtItemCount.setText(count + " İçerik");
        }
    }
}