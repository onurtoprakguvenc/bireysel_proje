package com.example.hadi_bakalm.model;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.SonIncelemeAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SonIncelemeActivity extends AppCompatActivity {

    private LinearLayout btnClearHistory;
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

        if (rvHistoryList != null) {
            rvHistoryList.setLayoutManager(new LinearLayoutManager(this));

            // Verileri Yükle
            tumListe = new ArrayList<>();
            tumListe.add(new SonIncelemeModel("Derin Çalışma Disiplini", "Dikkat dağıtıcı unsurlar olmadan odaklanmış çalı...", "45 dk önce", "Metin"));
            tumListe.add(new SonIncelemeModel("Batık Maliyet Yanılsaması", "Geçmişte harcanan zaman veya para yüzünden ...", "2 saat önce", "Kavram"));
            tumListe.add(new SonIncelemeModel("Stoacı Kabul İlkeleri", "Kontrol edemediğimiz olaylara karşı zihinsel ding...", "Dün", "Metin"));
            tumListe.add(new SonIncelemeModel("Pareto İlkesi (80/20 Rule)", "Sonuçların %80'inin çabaların %20'sinden kayn...", "2 gün önce", "Kavram"));

            adapter = new SonIncelemeAdapter(new ArrayList<>(tumListe), new SonIncelemeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(SonIncelemeModel item) {
                    // Detay sayfasına geçiş
                }

                @Override
                public void onDeleteClick(int position) {
                    tumListe.remove(position);
                    filterListCurrentState(); // Mevcut filtreye göre listeyi yenile
                }
            });

            rvHistoryList.setAdapter(adapter);
        }

        // Tıklama Dinleyicileri
        if (chipAll != null) chipAll.setOnClickListener(v -> filterList("Tümü"));
        if (chipConcepts != null) chipConcepts.setOnClickListener(v -> filterList("Kavram"));
        if (chipTexts != null) chipTexts.setOnClickListener(v -> filterList("Metin"));

        // Tümünü Temizle Butonu
        btnClearHistory = findViewById(R.id.btnClearHistory);

        if (btnClearHistory != null) {
            // ScrollView engelini aşmak için katmanı öne çekiyoruz
            btnClearHistory.bringToFront();

            btnClearHistory.setOnClickListener(v -> {
                if (tumListe == null || tumListe.isEmpty()) {
                    return;
                }

                new androidx.appcompat.app.AlertDialog.Builder(SonIncelemeActivity.this)
                        .setTitle("Geçmişi Temizle")
                        .setMessage("Tüm inceleme geçmişiniz silinecektir. Onaylıyor musunuz?")
                        .setPositiveButton("Temizle", (dialog, which) -> {
                            tumListe.clear();
                            if (adapter != null) {
                                adapter.updateList(new ArrayList<>());
                            }
                            updateChipCounts(0, 0, 0);
                        })
                        .setNegativeButton("Vazgeç", null)
                        .show();
            });
        }
    } // onCreate Metodunun Kapanışı

    private void filterListCurrentState() {
        filterList(currentFilter);
    }

    private void updateChipCounts(int total, int concepts, int texts) {
        if (chipAll != null && chipAll.getChildCount() > 1) {
            ((TextView) chipAll.getChildAt(1)).setText(String.valueOf(total));
        }
        if (chipConcepts != null && chipConcepts.getChildCount() > 2) {
            ((TextView) chipConcepts.getChildAt(2)).setText(String.valueOf(concepts));
        }
        if (chipTexts != null && chipTexts.getChildCount() > 2) {
            ((TextView) chipTexts.getChildAt(2)).setText(String.valueOf(texts));
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

    private void filterList(String type) {
        currentFilter = type;
        List<SonIncelemeModel> filteredList = new ArrayList<>();

        if ("Tümü".equalsIgnoreCase(type)) {
            filteredList.addAll(tumListe);
            updateChipUI(chipAll, true);
            updateChipUI(chipConcepts, false);
            updateChipUI(chipTexts, false);
        } else {
            for (SonIncelemeModel item : tumListe) {
                if (item.getTur().equalsIgnoreCase(type)) {
                    filteredList.add(item);
                }
            }

            if ("Kavram".equalsIgnoreCase(type)) {
                updateChipUI(chipAll, false);
                updateChipUI(chipConcepts, true);
                updateChipUI(chipTexts, false);
            } else if ("Metin".equalsIgnoreCase(type)) {
                updateChipUI(chipAll, false);
                updateChipUI(chipConcepts, false);
                updateChipUI(chipTexts, true);
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }
} // Class Kapanışı