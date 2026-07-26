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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SonIncelemeActivity extends AppCompatActivity {

    private LinearLayout btnClearHistory; // Tür düzeltildi
    private EditText etSearchHistory;
    private RecyclerView rvHistoryList;
    private SonIncelemeAdapter adapter;
    private List<SonIncelemeModel> tumListe;

    // Filtre Çipleri
    private LinearLayout chipAll, chipConcepts, chipTexts;
    private String currentFilter = "Tümü";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_son_inceleme);
        NavigationHelper.setupBottomNavigation(this);

        btnClearHistory = findViewById(R.id.btnClearHistory);
        etSearchHistory = findViewById(R.id.etSearchHistory);
        rvHistoryList = findViewById(R.id.rvHistoryList);

        chipAll = findViewById(R.id.chipAll);
        chipConcepts = findViewById(R.id.chipConcepts);
        chipTexts = findViewById(R.id.chipTexts);

        // 1. Verileri Yükle
        tumListe = new ArrayList<>();
        tumListe.add(new SonIncelemeModel("Derin Çalışma Disiplini", "Dikkat dağıtıcı unsurlar olmadan odaklanmış çalı...", "45 dk önce", "Metin"));
        tumListe.add(new SonIncelemeModel("Batık Maliyet Yanılsaması", "Geçmişte harcanan zaman veya para yüzünden ...", "2 saat önce", "Kavram"));
        tumListe.add(new SonIncelemeModel("Stoacı Kabul İlkeleri", "Kontrol edemediğimiz olaylara karşı zihinsel ding...", "Dün", "Metin"));
        tumListe.add(new SonIncelemeModel("Pareto İlkesi (80/20 Rule)", "Sonuçların %80'inin çabaların %20'sinden kayn...", "2 gün önce", "Kavram"));

        if (rvHistoryList != null) {
            rvHistoryList.setLayoutManager(new LinearLayoutManager(this));

            adapter = new SonIncelemeAdapter(new ArrayList<>(tumListe), new SonIncelemeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(SonIncelemeModel item) {
                    // Detay sayfasına geçiş
                }

                @Override
                public void onDeleteClick(int position) {
                    if (position >= 0 && position < tumListe.size()) {
                        tumListe.remove(position);
                        applyFilterAndSearch();
                    }
                }
            });

            rvHistoryList.setAdapter(adapter);
        }

        // 2. Çip Tıklama Dinleyicileri
        if (chipAll != null) chipAll.setOnClickListener(v -> filterList("Tümü"));
        if (chipConcepts != null) chipConcepts.setOnClickListener(v -> filterList("Kavram"));
        if (chipTexts != null) chipTexts.setOnClickListener(v -> filterList("Metin"));

        // 3. Arama Entegrasyonu
        setupSearch();

        // 4. Temizle Butonu Entegrasyonu
        setupClearButton();
    }

    private void setupClearButton() {
        if (btnClearHistory == null) {
            Log.e("SonIncelemeActivity", "btnClearHistory XML içerisinde bulunamadı!");
            return;
        }

        btnClearHistory.setOnClickListener(v -> {
            Log.d("SonIncelemeActivity", "Temizle butonuna basıldı.");

            if (tumListe == null || tumListe.isEmpty()) {
                Toast.makeText(SonIncelemeActivity.this, "Temizlenecek geçmiş bulunmuyor.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(SonIncelemeActivity.this)
                    .setTitle("Geçmişi Temizle")
                    .setMessage("Tüm inceleme geçmişiniz silinecektir. Onaylıyor musunuz?")
                    .setPositiveButton("Temizle", (dialog, which) -> {
                        tumListe.clear();
                        applyFilterAndSearch();
                        updateChipCounts(0, 0, 0);
                        Toast.makeText(SonIncelemeActivity.this, "Geçmiş temizlendi.", Toast.LENGTH_SHORT).show();
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
        // chipAll içerisinde tek TextView olduğu için getChildAt(0) kontrol ediliyor
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