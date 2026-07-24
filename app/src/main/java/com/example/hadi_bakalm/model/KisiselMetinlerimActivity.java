package com.example.hadi_bakalm.model;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter; // veya kendi adapter sınıfın
import com.example.hadi_bakalm.model.kaydedilenler;

import java.util.ArrayList;
import java.util.List;

public class KisiselMetinlerimActivity extends AppCompatActivity {

    private TextView btnAll, btnCat1, btnCat2, btnCat3;
    private RecyclerView recyclerView;
    private kaydedilenler_adapter adapter;
    private List<kaydedilenler> masterList;
    private List<kaydedilenler> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ana_sayfa_kisisel_metinlerim);

        // Geri Butonu
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Kategori Buton Tanımlamaları (XML id'lerine göre)
        btnAll = findViewById(R.id.tumu);
        btnCat1 = findViewById(R.id.kategori_1);
        btnCat2 = findViewById(R.id.kategori_2);
        btnCat3 = findViewById(R.id.kategori_3);

        recyclerView = findViewById(R.id.recyclerViewPersonalTexts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tüm Veri Havuzunu Oluşturuyoruz
        setupDummyData();

        // Varsayılan olarak "Tümü" seçili gelsin
        filterCategory("TÜMÜ", btnAll);

        // Tıklama Olayları
        btnAll.setOnClickListener(v -> filterCategory("TÜMÜ", btnAll));
        btnCat1.setOnClickListener(v -> filterCategory("Kategori 1", btnCat1));
        btnCat2.setOnClickListener(v -> filterCategory("Kategori 2", btnCat2));
        btnCat3.setOnClickListener(v -> filterCategory("Kategori 3", btnCat3));
    }

    private void setupDummyData() {
        masterList = new ArrayList<>();

        // Kategori 1 İçerikleri (3 Tane)
        masterList.add(new kaydedilenler("1", "Nöroplastisite Notları", "Beynin yapısının deneyimlerle değişimi.", "METİN", "Kategori 1", "Dün eklendi"));
        masterList.add(new kaydedilenler("2", "Sinaptik Budanma", "Kullanılmayan bağlantıların temizlenmesi.", "METİN", "Kategori 1", "2 gün önce"));
        masterList.add(new kaydedilenler("3", "Miyelinleşme Süreci", "Hızlı bilgi iletimi ve derinleşme.", "METİN", "Kategori 1", "Geçen hafta"));

        // Kategori 2 İçerikleri (3 Tane)
        masterList.add(new kaydedilenler("4", "Bilişsel Haritalar", "Mekansal hafıza ve öğrenme süreçleri.", "METİN", "Kategori 2", "3 gün önce"));
        masterList.add(new kaydedilenler("5", "Zihinsel Modeller", "Karmaşık durumları basitleştirme sanatı.", "METİN", "Kategori 2", "5 gün önce"));
        masterList.add(new kaydedilenler("6", "Çerçeveleme Etkisi", "Bilginin sunuluş biçiminin kararlara etkisi.", "METİN", "Kategori 2", "1 hafta önce"));

        // Kategori 3 İçerikleri (3 Tane)
        masterList.add(new kaydedilenler("7", "Odaklanma Protokolü", "Günlük çalışma ve dikkat süreleri.", "METİN", "Kategori 3", "Dün eklendi"));
        masterList.add(new kaydedilenler("8", "Sirkadiyen Ritim", "Biyolojik saat ve verim pencereleri.", "METİN", "Kategori 3", "4 gün önce"));
        masterList.add(new kaydedilenler("9", "Dopamin Detoksu", "Reseptör duyarlılığını yeniden kazanma.", "METİN", "Kategori 3", "2 hafta önce"));

        displayList = new ArrayList<>();
    }

    private void filterCategory(String categoryName, TextView selectedButton) {
        // 1. Buton Renklerini Sıfırla ve Seçili Olana Siyah Arka Plan Ver
        resetButtonStyles();
        selectedButton.setBackgroundResource(R.drawable.bg_black_icon_box); // Siyah oval arka plan
        selectedButton.setTextColor(Color.WHITE);

        // 2. Listeyi Süz
        displayList.clear();
        if (categoryName.equals("TÜMÜ")) {
            displayList.addAll(masterList);
        } else {
            for (kaydedilenler item : masterList) {
                if (item.getCategory().equalsIgnoreCase(categoryName)) {
                    displayList.add(item);
                }
            }
        }

        // 3. Adapter Bağla / Güncelle
        adapter = new kaydedilenler_adapter(this, displayList);
        recyclerView.setAdapter(adapter);
    }

    private void resetButtonStyles() {
        TextView[] buttons = {btnAll, btnCat1, btnCat2, btnCat3};
        for (TextView btn : buttons) {
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.bg_kaydet_button); // Açık gri oval arka plan
                btn.setTextColor(Color.parseColor("#475569")); // Gri yazı
            }
        }
    }
}