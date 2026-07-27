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

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;
import com.example.hadi_bakalm.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class kaydet_ana_sayfa extends AppCompatActivity {

    private RecyclerView recyclerViewSaved;
    private kaydedilenler_adapter adapter;
    private List<kaydedilenler> savedList;
    private List<kaydedilenler> currentFilteredList;
    private TextView txtItemCount;
    private ImageView btnBack;
    private EditText etSearch;

    private TextView btnFilterAll, btnFilterConcepts, btnFilterTexts;
    private String selectedType = "ALL";

    // Room Veri Tabanı Bağlantısı
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kaydet_sayfa_gorme);

        db = AppDatabase.getInstance(this);

        NavigationHelper.setupBottomNavigation(this);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        setupSearchAndFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Detay sayfasından kaydedildi/çıkarıldı yapılıp dönüldüğünde verileri tazeler
        loadSavedDataFromDb();
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

        savedList = new ArrayList<>();
        currentFilteredList = new ArrayList<>();

        adapter = new kaydedilenler_adapter(this, currentFilteredList);
        recyclerViewSaved.setAdapter(adapter);

        // Verileri ilk kez yüklüyoruz
        loadSavedDataFromDb();
    }

    // --- ROOM VERİ TABANINDAN DİNAMİK VERİ ÇEKME METODU ---
    private void loadSavedDataFromDb() {
        if (db == null) return;

        // Veri tabanından tüm verileri çekiyoruz
        List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();

        savedList.clear();

        if (allConcepts != null) {
            for (ConceptItem_kavram item : allConcepts) {
                // Sadece "isSaved == true" olan yani kaydedilmiş verileri süzüyoruz
                if (item.isSaved()) {
                    kaydedilenler savedItem = new kaydedilenler(
                            String.valueOf(item.getId()),
                            item.getTitle(),
                            item.getDescription(),
                            "KAVRAM", // Tür olarak KAVRAM atanıyor
                            "Kaydedilen Kavram",
                            "Kayıtlı"
                    );
                    savedList.add(savedItem);
                }
            }
        }

        // Arama ve filtreleme durumuna göre listeyi ekrana yansıtıyoruz
        applyFilterAndSearch();
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