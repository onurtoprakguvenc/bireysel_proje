package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.MetinItem;

public class kisisel_metin_okuma_sayfa extends AppCompatActivity {

    private float currentFontSizeSp = 16f;
    private AppDatabase db;
    private MetinItem currentMetin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        // Veri tabanı bağlantısı
        db = AppDatabase.getInstance(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSettings = findViewById(R.id.btnSettings);
        View cardSettingsPanel = findViewById(R.id.cardSettingsPanel);

        TextView txtBaslik = findViewById(R.id.baslık);
        TextView txtAciklama = findViewById(R.id.txtMainContent);
        EditText etPersonalNote = findViewById(R.id.etPersonalNote);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Ayarlar Butonu Tıklaması
        if (btnSettings != null && cardSettingsPanel != null) {
            btnSettings.setOnClickListener(v -> {
                int currentVisibility = cardSettingsPanel.getVisibility();
                if (currentVisibility == View.VISIBLE) {
                    cardSettingsPanel.setVisibility(View.GONE);
                } else {
                    cardSettingsPanel.setVisibility(View.VISIBLE);
                }
            });
        }

        // Intent verilerini alma ve Veri Tabanı Kaydı Başlatma
        Intent intent = getIntent();
        String title = "Varsayılan Başlık";
        String description = "Varsayılan İçerik";

        if (intent != null) {
            if (intent.getStringExtra("TITLE") != null) {
                title = intent.getStringExtra("TITLE");
            }
            if (intent.getStringExtra("DESCRIPTION") != null) {
                description = intent.getStringExtra("DESCRIPTION");
            }
        }

        if (txtBaslik != null) txtBaslik.setText(title);
        if (txtAciklama != null) txtAciklama.setText(description);

        // Veri tabanında bu başlıkta metin var mı kontrolü
        // Yoksa yeni oluştur, varsa çek
        checkAndLoadDatabase(title, description, etPersonalNote);

        // Not alanında değişiklik yapıldıkça canlı olarak veri tabanına yaz
        if (etPersonalNote != null) {
            etPersonalNote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentMetin != null) {
                        currentMetin.setPersonalNote(s.toString());
                        db.metinDao().update(currentMetin);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void checkAndLoadDatabase(String title, String description, EditText etPersonalNote) {
        // Test amaçlı: İlk metni getir yoksa oluştur
        java.util.List<MetinItem> list = db.metinDao().getAllMetinler();
        if (list.isEmpty()) {
            currentMetin = new MetinItem(title, description, "", false);
            db.metinDao().insert(currentMetin);
            // Insert sonrası eklenen veriyi id ile almak için tekrar liste çekilir
            list = db.metinDao().getAllMetinler();
            if (!list.isEmpty()) {
                currentMetin = list.get(0);
            }
        } else {
            currentMetin = list.get(0);
            if (etPersonalNote != null && currentMetin.getPersonalNote() != null) {
                etPersonalNote.setText(currentMetin.getPersonalNote());
            }
        }
    }
}