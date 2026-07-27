package com.example.hadi_bakalm.model;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SonIncelemeActivity extends AppCompatActivity {

    private LinearLayout btnClearHistory;
    private EditText etSearchHistory;
    private RecyclerView rvHistoryList;
    private SonIncelemeAdapter adapter;
    private List<SonIncelemeModel> tumListe;

    // Filtre Çipleri
    private LinearLayout chipAll, chipConcepts, chipTexts;
    private String currentFilter = "Tümü";

    // Room Veri Tabanı Bağlantısı
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_son_inceleme);
        NavigationHelper.setupBottomNavigation(this);

        db = AppDatabase.getInstance(this);

        btnClearHistory = findViewById(R.id.btnClearHistory);
        etSearchHistory = findViewById(R.id.etSearchHistory);
        rvHistoryList = findViewById(R.id.rvHistoryList);

        chipAll = findViewById(R.id.chipAll);
        chipConcepts = findViewById(R.id.chipConcepts);
        chipTexts = findViewById(R.id.chipTexts);

        tumListe = new ArrayList<>();

        if (rvHistoryList != null) {
            rvHistoryList.setLayoutManager(new LinearLayoutManager(this));

            adapter = new SonIncelemeAdapter(new ArrayList<>(), new SonIncelemeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(SonIncelemeModel item) {
                    // Detay sayfasına geçiş tıklama mantığı
                }

                @Override
                public void onDeleteClick(SonIncelemeModel item) {
                    if (item != null && tumListe != null) {
                        tumListe.remove(item);
                        applyFilterAndSearch();
                    }
                }
            });

            rvHistoryList.setAdapter(adapter);
        }

        // Çip Tıklama Dinleyicileri
        if (chipAll != null) chipAll.setOnClickListener(v -> filterList("Tümü"));
        if (chipConcepts != null) chipConcepts.setOnClickListener(v -> filterList("Kavram"));
        if (chipTexts != null) chipTexts.setOnClickListener(v -> filterList("Metin"));

        // Arama ve Temizle Entegrasyonları
        setupSearch();
        setupClearButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryDataFromDb();
    }

    // --- ROOM VERİ TABANINDAN DİNAMİK VERİ ÇEKME METODU ---
    private void loadHistoryDataFromDb() {
        if (db == null) return;

        new Thread(() -> {
            List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
            List<SonIncelemeModel> gecmisListesi = new ArrayList<>();

            if (allConcepts != null) {
                for (ConceptItem_kavram item : allConcepts) {
                    // SADECE GİRİLİP İNCELENMİŞ OLANLARI ALIYORUZ (lastViewedTime > 0)
                    if (item.getLastViewedTime() > 0) {

                        // Zamanı okunabilir formata çevirebiliriz (Örn: "Son incelendi")
                        SonIncelemeModel modelItem = new SonIncelemeModel(
                                item.getId(),
                                item.getTitle(),
                                item.getDescription(),
                                "Son incelendi",
                                "Kavram"
                        );
                        gecmisListesi.add(modelItem);
                    }
                }
            }

            // Arayüzü ana izde güncelliyoruz
            runOnUiThread(() -> {
                tumListe.clear();
                tumListe.addAll(gecmisListesi);
                applyFilterAndSearch();
            });
        }).start();
    }

    private void setupClearButton() {
        if (btnClearHistory == null) return;

        btnClearHistory.setOnClickListener(v -> {
            if (tumListe == null || tumListe.isEmpty()) {
                Toast.makeText(SonIncelemeActivity.this, "Temizlenecek geçmiş bulunmuyor.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(SonIncelemeActivity.this)
                    .setTitle("Geçmişi Temizle")
                    .setMessage("Tüm inceleme geçmişiniz silinecektir. Onaylıyor musunuz?")
                    .setPositiveButton("Temizle", (dialog, which) -> {

                        new Thread(() -> {
                            if (db != null) {
                                // Doğrudan DAO'daki deleteAll çalışacak
                                db.conceptDao_kavram().deleteAll();
                            }

                            runOnUiThread(() -> {
                                tumListe.clear();
                                applyFilterAndSearch();
                                updateChipCounts(0, 0, 0);
                                Toast.makeText(SonIncelemeActivity.this, "Geçmiş temizlendi.", Toast.LENGTH_SHORT).show();
                            });
                        }).start();

                    })
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
        Locale trLocale = new Locale("tr", "TR");
        String query = etSearchHistory != null ? etSearchHistory.getText().toString().toLowerCase(trLocale).trim() : "";

        List<SonIncelemeModel> filteredList = new ArrayList<>();

        int conceptsCount = 0;
        int textsCount = 0;

        for (SonIncelemeModel item : tumListe) {
            if ("Kavram".equalsIgnoreCase(item.getTur())) conceptsCount++;
            if ("Metin".equalsIgnoreCase(item.getTur())) textsCount++;

            boolean matchesType = "Tümü".equalsIgnoreCase(currentFilter) || item.getTur().equalsIgnoreCase(currentFilter);

            String baslik = item.getBaslik() != null ? item.getBaslik().toLowerCase(trLocale) : "";
            String aciklama = item.getAciklama() != null ? item.getAciklama().toLowerCase(trLocale) : "";

            boolean matchesQuery = baslik.contains(query) || aciklama.contains(query);

            if (matchesType && matchesQuery) {
                filteredList.add(item);
            }
        }

        updateChipCounts(tumListe.size(), conceptsCount, textsCount);

        if ("Tümü".equalsIgnoreCase(currentFilter)) {
            updateChipUI(chipAll, true);
            updateChipUI(chipConcepts, false);
            updateChipUI(chipTexts, false);
        } else if ("Kavram".equalsIgnoreCase(currentFilter)) {
            updateChipUI(chipAll, false);
            updateChipUI(chipConcepts, true);
            updateChipUI(chipTexts, false);
        } else if ("Metin".equalsIgnoreCase(currentFilter)) {
            updateChipUI(chipAll, false);
            updateChipUI(chipConcepts, false);
            updateChipUI(chipTexts, true);
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void updateChipCounts(int total, int concepts, int texts) {
        if (chipAll != null && chipAll.getChildCount() > 0) {
            View v = chipAll.getChildAt(0);
            if (v instanceof TextView) {
                ((TextView) v).setText("Tümü (" + total + ")");
            }
        }

        if (chipConcepts != null) {
            for (int i = 0; i < chipConcepts.getChildCount(); i++) {
                View v = chipConcepts.getChildAt(i);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (!tv.getText().toString().equals(getString(R.string.kavramlar))) {
                        tv.setText(String.valueOf(concepts));
                    }
                }
            }
        }

        if (chipTexts != null) {
            for (int i = 0; i < chipTexts.getChildCount(); i++) {
                View v = chipTexts.getChildAt(i);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (!tv.getText().toString().equals(getString(R.string.metinler))) {
                        tv.setText(String.valueOf(texts));
                    }
                }
            }
        }
    }

    private void updateChipUI(LinearLayout chip, boolean isActive) {
        if (chip == null) return;

        TextView txtTitle = null;
        TextView txtCount = null;
        ImageView imgIcon = null;

        for (int i = 0; i < chip.getChildCount(); i++) {
            View v = chip.getChildAt(i);
            if (v instanceof ImageView) {
                imgIcon = (ImageView) v;
            } else if (v instanceof TextView) {
                if (txtTitle == null) {
                    txtTitle = (TextView) v;
                } else {
                    txtCount = (TextView) v;
                }
            }
        }

        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            if (txtTitle != null) txtTitle.setTextColor(Color.parseColor("#FFFFFF"));
            if (txtCount != null) txtCount.setTextColor(Color.parseColor("#94A3B8"));
            if (imgIcon != null) imgIcon.setColorFilter(Color.parseColor("#FFFFFF"));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            if (txtTitle != null) txtTitle.setTextColor(Color.parseColor("#334155"));
            if (txtCount != null) txtCount.setTextColor(Color.parseColor("#64748B"));
            if (imgIcon != null) imgIcon.setColorFilter(Color.parseColor("#64748B"));
        }
    }
}