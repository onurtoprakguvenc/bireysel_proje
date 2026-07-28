package com.example.hadi_bakalm.model;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.kaydedilenler;

import java.util.ArrayList;
import java.util.List;

public class KisiselMetinlerimActivity extends AppCompatActivity {

    private TextView btnAll;
    private RecyclerView recyclerView;
    private kaydedilenler_adapter adapter;
    private List<kaydedilenler> masterList;
    private List<kaydedilenler> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ana_sayfa_kisisel_metinlerim);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAll = findViewById(R.id.tumu);

        recyclerView = findViewById(R.id.recyclerViewPersonalTexts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        displayList = new ArrayList<>();

        if (btnAll != null) {
            btnAll.setOnClickListener(v -> filterCategory("TÜMÜ", btnAll));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sayfaya her girildiğinde Room veritabanından güncel kayıt durumlarını yükle
        loadDataFromRoomDatabase();
    }

    private void loadDataFromRoomDatabase() {
        // Geçici liste şablonumuzu oluşturuyoruz
        List<kaydedilenler> rawList = new ArrayList<>();
        rawList.add(new kaydedilenler("1", "Nöroplastisite Notları", "Beynin yapısının deneyimlerle değişimi.", "METİN", "", "Dün eklendi", false));
        rawList.add(new kaydedilenler("2", "Sinaptik Budanma", "Kullanılmayan bağlantıların temizlenmesi.", "METİN", "", "2 gün önce", false));
        rawList.add(new kaydedilenler("3", "Miyelinleşme Süreci", "Hızlı bilgi iletimi ve derinleşme.", "METİN", "", "Geçen hafta", false));
        rawList.add(new kaydedilenler("4", "Bilişsel Haritalar", "Mekansal hafıza ve öğrenme süreçleri.", "METİN", "", "3 gün önce", false));
        rawList.add(new kaydedilenler("5", "Zihinsel Modeller", "Karmaşık durumları basitleştirme sanatı.", "METİN", "", "5 gün önce", false));
        rawList.add(new kaydedilenler("6", "Çerçeveleme Etkisi", "Bilginin sunuluş biçiminin kararlara etkisi.", "METİN", "", "1 hafta önce", false));

        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            List<ConceptItem_kavram> dbConcepts = db.conceptDao_kavram().getAllConceptler();

            if (dbConcepts != null && !dbConcepts.isEmpty()) {
                for (kaydedilenler item : rawList) {
                    for (ConceptItem_kavram concept : dbConcepts) {
                        if (concept.getTitle() != null && concept.getTitle().equalsIgnoreCase(item.getTitle())) {
                            // Veritabanındaki isSaved durumunu modele aktar
                            item.setSaved(concept.isSaved());
                            break;
                        }
                    }
                }
            }

            // Arayüzü ana iş parçacığında (Main Thread) güncelle
            runOnUiThread(() -> {
                masterList = rawList;
                filterCategory("TÜMÜ", btnAll);
            });
        }).start();
    }

    private void filterCategory(String categoryName, TextView selectedButton) {
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_black_icon_box);
            selectedButton.setTextColor(Color.WHITE);
        }

        if (masterList != null) {
            displayList.clear();
            displayList.addAll(masterList);

            adapter = new kaydedilenler_adapter(this, displayList);
            recyclerView.setAdapter(adapter);
        }
    }
}