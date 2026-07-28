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

        setupDummyData();

        filterCategory("TÜMÜ", btnAll);

        if (btnAll != null) {
            btnAll.setOnClickListener(v -> filterCategory("TÜMÜ", btnAll));
        }
    }

    private void setupDummyData() {
        masterList = new ArrayList<>();

        // Son parametre isSaved (true = kaydedilmiş/koyu ikon, false = kaydedilmemiş/açık ikon)
        masterList.add(new kaydedilenler("1", "Nöroplastisite Notları", "Beynin yapısının deneyimlerle değişimi.", "METİN", "", "Dün eklendi", false));
        masterList.add(new kaydedilenler("2", "Sinaptik Budanma", "Kullanılmayan bağlantıların temizlenmesi.", "METİN", "", "2 gün önce", false));
        masterList.add(new kaydedilenler("3", "Miyelinleşme Süreci", "Hızlı bilgi iletimi ve derinleşme.", "METİN", "", "Geçen hafta", false));
        masterList.add(new kaydedilenler("4", "Bilişsel Haritalar", "Mekansal hafıza ve öğrenme süreçleri.", "METİN", "", "3 gün önce", false));
        masterList.add(new kaydedilenler("5", "Zihinsel Modeller", "Karmaşık durumları basitleştirme sanatı.", "METİN", "", "5 gün önce", false));
        masterList.add(new kaydedilenler("6", "Çerçeveleme Etkisi", "Bilginin sunuluş biçiminin kararlara etkisi.", "METİN", "", "1 hafta önce", false));

        displayList = new ArrayList<>();
    }

    private void filterCategory(String categoryName, TextView selectedButton) {
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_black_icon_box);
            selectedButton.setTextColor(Color.WHITE);
        }

        displayList.clear();
        displayList.addAll(masterList);

        adapter = new kaydedilenler_adapter(this, displayList);
        recyclerView.setAdapter(adapter);
    }
}