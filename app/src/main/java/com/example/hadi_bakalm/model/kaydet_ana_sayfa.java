package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;
import com.example.hadi_bakalm.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class kaydet_ana_sayfa extends AppCompatActivity {

    public static final String TYPE_KAVRAM = "KAVRAM";
    public static final String TYPE_METIN = "METIN";

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Locale TR_LOCALE = new Locale("tr", "TR");

    private RecyclerView recyclerViewSaved;
    private kaydedilenler_adapter adapter;

    private final List<kaydedilenler> savedList = new ArrayList<>();
    private final List<kaydedilenler> currentFilteredList = new ArrayList<>();

    private EditText etSearch;

    private TextView btnFilterAll;
    private TextView btnFilterConcepts;
    private TextView btnFilterTexts;
    private String selectedType = "ALL";

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kaydet_sayfa_gorme);

        db = AppDatabase.getInstance(this);

        try {
            NavigationHelper.setupBottomNavigation(this);
        } catch (Exception ignored) {}

        initViews();
        setupClickListeners();
        setupRecyclerView();
        setupSearchAndFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedDataFromDb();
    }

    private void initViews() {
        recyclerViewSaved = findViewById(R.id.recyclerViewSaved);
        etSearch = findViewById(R.id.etSearch);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterConcepts = findViewById(R.id.btnFilterConcepts);
        btnFilterTexts = findViewById(R.id.btnFilterTexts);
    }

    private void setupClickListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(kaydet_ana_sayfa.this, EskiMainActivity.class);
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
                selectedType = TYPE_KAVRAM;
                updateFilterUI();
                applyFilterAndSearch();
            });
        }

        if (btnFilterTexts != null) {
            btnFilterTexts.setOnClickListener(v -> {
                selectedType = TYPE_METIN;
                updateFilterUI();
                applyFilterAndSearch();
            });
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewSaved != null) {
            recyclerViewSaved.setLayoutManager(new LinearLayoutManager(this));
            adapter = new kaydedilenler_adapter(this, currentFilteredList);
            recyclerViewSaved.setAdapter(adapter);
        }
    }

    private void loadSavedDataFromDb() {
        if (db == null) return;

        DB_EXECUTOR.execute(() -> {
            List<kaydedilenler> tempSavedList = new ArrayList<>();

            // 1. KAVRAMLAR
            List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
            if (allConcepts != null) {
                for (ConceptItem_kavram item : allConcepts) {
                    if (item != null && item.isSaved()) {
                        kaydedilenler savedItem = new kaydedilenler(
                                String.valueOf(item.getId()),
                                item.getTitle(),
                                item.getDescription(),
                                TYPE_KAVRAM,
                                "",
                                "Dün eklendi",
                                true
                        );
                        tempSavedList.add(savedItem);
                    }
                }
            }

            // 2. METİNLER
            List<MetinItem> allMetinler = db.metinDao().getAllMetinler();
            if (allMetinler != null) {
                for (MetinItem item : allMetinler) {
                    if (item != null && item.isSaved()) {
                        kaydedilenler savedItem = new kaydedilenler(
                                String.valueOf(item.getId()),
                                item.getTitle(),
                                item.getContent(),
                                TYPE_METIN,
                                "",
                                "Dün eklendi",
                                true
                        );
                        tempSavedList.add(savedItem);
                    }
                }
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                savedList.clear();
                savedList.addAll(tempSavedList);
                applyFilterAndSearch();
            });
        });
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
        String query = etSearch != null && etSearch.getText() != null
                ? etSearch.getText().toString().toLowerCase(TR_LOCALE).trim()
                : "";

        List<kaydedilenler> filtered = new ArrayList<>();

        for (kaydedilenler item : savedList) {
            if (item == null) continue;

            String itemType = item.getType() != null ? item.getType().trim() : "";
            boolean matchesType = "ALL".equalsIgnoreCase(selectedType) || itemType.equalsIgnoreCase(selectedType);

            String baslik = item.getTitle() != null ? item.getTitle().toLowerCase(TR_LOCALE) : "";
            String aciklama = item.getDescription() != null ? item.getDescription().toLowerCase(TR_LOCALE) : "";
            String icerik = item.getContent() != null ? item.getContent().toLowerCase(TR_LOCALE) : "";

            boolean matchesQuery = query.isEmpty()
                    || baslik.contains(query)
                    || aciklama.contains(query)
                    || icerik.contains(query);

            if (matchesType && matchesQuery) {
                filtered.add(item);
            }
        }

        currentFilteredList.clear();
        currentFilteredList.addAll(filtered);

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (adapter != null) {
                adapter.filterList(new ArrayList<>(currentFilteredList));
            }
        });
    }

    private void updateFilterUI() {
        setFilterButtonState(btnFilterAll, selectedType.equals("ALL"));
        setFilterButtonState(btnFilterConcepts, selectedType.equals(TYPE_KAVRAM));
        setFilterButtonState(btnFilterTexts, selectedType.equals(TYPE_METIN));
    }

    private void setFilterButtonState(TextView btn, boolean isSelected) {
        if (btn == null) return;
        if (isSelected) {
            btn.setBackgroundResource(R.drawable.bg_black_pill);
            btn.setTextColor(Color.WHITE);
        } else {
            btn.setBackgroundResource(R.drawable.bg_chip_inactive);
            btn.setTextColor(Color.parseColor("#475569"));
        }
    }
}