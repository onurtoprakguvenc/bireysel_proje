package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.SonIncelemeAdapter;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.kisisel_metin_okuma_sayfa;
import com.example.hadi_bakalm.noroplastite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SonIncelemeActivity extends AppCompatActivity {

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Locale TR_LOCALE = new Locale("tr", "TR");

    private LinearLayout btnClearHistory;
    private EditText etSearchHistory;
    private SonIncelemeAdapter adapter;
    private final List<SonIncelemeModel> tumListe = new ArrayList<>();

    private LinearLayout chipAll;
    private LinearLayout chipConcepts;
    private LinearLayout chipTexts;
    private String currentFilter = "Tümü";

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_son_inceleme);
        NavigationHelper.setupBottomNavigation(this);

        db = AppDatabase.getInstance(this);

        initViews();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupClearButton();
    }

    private void initViews() {
        btnClearHistory = findViewById(R.id.btnClearHistory);
        etSearchHistory = findViewById(R.id.etSearchHistory);
        chipAll = findViewById(R.id.chipAll);
        chipConcepts = findViewById(R.id.chipConcepts);
        chipTexts = findViewById(R.id.chipTexts);
    }

    private void setupRecyclerView() {
        RecyclerView rvHistoryList = findViewById(R.id.rvHistoryList);
        if (rvHistoryList == null) return;

        rvHistoryList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SonIncelemeAdapter(new ArrayList<>(), new SonIncelemeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SonIncelemeModel item) {
                if (item == null || isFinishing() || isDestroyed()) return;

                Intent intent;
                if ("Metin".equalsIgnoreCase(item.getTur())) {
                    intent = new Intent(SonIncelemeActivity.this, kisisel_metin_okuma_sayfa.class);
                    intent.putExtra("ID", (int) item.getId());
                    intent.putExtra("TITLE", item.getBaslik());
                    intent.putExtra("DESCRIPTION", item.getAciklama());
                    intent.putExtra("CONTENT", item.getAciklama());
                } else {
                    // Kavram detay sayfasına (noroplastite) tüm anahtarlar eksiksiz aktarılıyor
                    intent = new Intent(SonIncelemeActivity.this, noroplastite.class);
                    intent.putExtra("KAVRAM_ID", String.valueOf(item.getId()));
                    intent.putExtra("KAVRAM_ADI", item.getBaslik());
                    intent.putExtra("KAVRAM_ACIKLAMA", item.getAciklama());
                    intent.putExtra("KAVRAM_ICERIK", item.getAciklama());
                    intent.putExtra("TITLE", item.getBaslik());
                    intent.putExtra("DESCRIPTION", item.getAciklama());
                }

                startActivity(intent);
            }

            @Override
            public void onDeleteClick(SonIncelemeModel item) {
                if (item == null) return;

                tumListe.remove(item);
                applyFilterAndSearch();

                DB_EXECUTOR.execute(() -> {
                    if (db == null) return;
                    int itemId = (int) item.getId();

                    if ("Kavram".equalsIgnoreCase(item.getTur())) {
                        ConceptItem_kavram concept = db.conceptDao_kavram().getConceptById(itemId);
                        if (concept != null) {
                            concept.setLastViewedTime(0);
                            db.conceptDao_kavram().update(concept);
                        }
                    } else {
                        MetinItem metin = db.metinDao().getMetinById(itemId);
                        if (metin != null) {
                            metin.setLastViewedTime(0);
                            db.metinDao().update(metin);
                        }
                    }
                });
            }
        });

        rvHistoryList.setAdapter(adapter);
    }

    private void setupFilterChips() {
        if (chipAll != null) chipAll.setOnClickListener(v -> filterList("Tümü"));
        if (chipConcepts != null) chipConcepts.setOnClickListener(v -> filterList("Kavram"));
        if (chipTexts != null) chipTexts.setOnClickListener(v -> filterList("Metin"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryDataFromDb();
    }

    private void loadHistoryDataFromDb() {
        if (db == null) return;

        DB_EXECUTOR.execute(() -> {
            List<SonIncelemeModel> gecmisListesi = new ArrayList<>();

            List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
            if (allConcepts != null) {
                for (ConceptItem_kavram item : allConcepts) {
                    if (item != null && item.getLastViewedTime() > 0) {
                        gecmisListesi.add(new SonIncelemeModel(
                                item.getId(),
                                item.getTitle(),
                                item.getDescription(),
                                "Son incelendi",
                                "Kavram"
                        ));
                    }
                }
            }

            List<MetinItem> metinler = db.metinDao().getRecentMetinler();
            if (metinler != null) {
                for (MetinItem item : metinler) {
                    if (item != null) {
                        gecmisListesi.add(new SonIncelemeModel(
                                item.getId(),
                                item.getTitle(),
                                item.getContent(),
                                "Son incelendi",
                                "Metin"
                        ));
                    }
                }
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                tumListe.clear();
                tumListe.addAll(gecmisListesi);
                applyFilterAndSearch();
            });
        });
    }

    private void setupClearButton() {
        if (btnClearHistory == null) return;

        btnClearHistory.setOnClickListener(v -> {
            if (tumListe.isEmpty()) {
                Toast.makeText(this, "Temizlenecek geçmiş bulunmuyor.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Geçmişi Temizle")
                    .setMessage("Tüm inceleme geçmişiniz temizlenecektir. Kaydedilen içerikleriniz silinmez. Onaylıyor musunuz?")
                    .setPositiveButton("Temizle", (dialog, which) -> DB_EXECUTOR.execute(() -> {
                        if (db != null) {
                            List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
                            if (allConcepts != null) {
                                for (ConceptItem_kavram concept : allConcepts) {
                                    if (concept != null && concept.getLastViewedTime() > 0) {
                                        concept.setLastViewedTime(0);
                                        db.conceptDao_kavram().update(concept);
                                    }
                                }
                            }

                            List<MetinItem> allMetinler = db.metinDao().getRecentMetinler();
                            if (allMetinler != null) {
                                for (MetinItem metin : allMetinler) {
                                    if (metin != null && metin.getLastViewedTime() > 0) {
                                        metin.setLastViewedTime(0);
                                        db.metinDao().update(metin);
                                    }
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            tumListe.clear();
                            applyFilterAndSearch();
                            Toast.makeText(this, "İnceleme geçmişi temizlendi.", Toast.LENGTH_SHORT).show();
                        });
                    }))
                    .setNegativeButton("Vazgeç", null)
                    .show();
        });
    }

    private void setupSearch() {
        if (etSearchHistory == null) return;

        etSearchHistory.addTextChangedListener(new TextWatcher() {
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

    private void filterList(String type) {
        currentFilter = type;
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        String query = etSearchHistory != null ? etSearchHistory.getText().toString().toLowerCase(TR_LOCALE).trim() : "";

        List<SonIncelemeModel> filteredList = new ArrayList<>();
        int conceptsCount = 0;
        int textsCount = 0;

        for (SonIncelemeModel item : tumListe) {
            if (item == null || item.getTur() == null) continue;

            if ("Kavram".equalsIgnoreCase(item.getTur())) conceptsCount++;
            if ("Metin".equalsIgnoreCase(item.getTur())) textsCount++;

            boolean matchesType = "Tümü".equalsIgnoreCase(currentFilter) || item.getTur().equalsIgnoreCase(currentFilter);
            String baslik = item.getBaslik() != null ? item.getBaslik().toLowerCase(TR_LOCALE) : "";
            String aciklama = item.getAciklama() != null ? item.getAciklama().toLowerCase(TR_LOCALE) : "";

            boolean matchesQuery = query.isEmpty() || baslik.contains(query) || aciklama.contains(query);

            if (matchesType && matchesQuery) {
                filteredList.add(item);
            }
        }

        updateChipCountsAndUI(tumListe.size(), conceptsCount, textsCount);

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void updateChipCountsAndUI(int total, int concepts, int texts) {
        updateSingleChip(chipAll, "Tümü".equalsIgnoreCase(currentFilter), String.valueOf(total));
        updateSingleChip(chipConcepts, "Kavram".equalsIgnoreCase(currentFilter), String.valueOf(concepts));
        updateSingleChip(chipTexts, "Metin".equalsIgnoreCase(currentFilter), String.valueOf(texts));
    }

    private void updateSingleChip(LinearLayout chip, boolean isActive, String countText) {
        if (chip == null) return;

        chip.setBackgroundResource(isActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);

        TextView countView = null;
        TextView titleView = null;
        ImageView iconView = null;

        for (int i = 0; i < chip.getChildCount(); i++) {
            View child = chip.getChildAt(i);
            if (child instanceof ImageView) {
                iconView = (ImageView) child;
            } else if (child instanceof TextView) {
                if (titleView == null) {
                    titleView = (TextView) child;
                } else {
                    countView = (TextView) child;
                }
            }
        }

        if (countView != null) {
            countView.setText(countText);
            countView.setTextColor(isActive ? Color.parseColor("#94A3B8") : Color.parseColor("#64748B"));
        }

        if (titleView != null) {
            titleView.setTextColor(isActive ? Color.parseColor("#FFFFFF") : Color.parseColor("#334155"));
        }
        if (iconView != null) {
            iconView.setColorFilter(isActive ? Color.parseColor("#FFFFFF") : Color.parseColor("#64748B"));
        }
    }
}