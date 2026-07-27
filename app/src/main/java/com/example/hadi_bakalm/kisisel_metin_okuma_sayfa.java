package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.MetinItem;

import java.util.List;

public class kisisel_metin_okuma_sayfa extends AppCompatActivity {

    private AppDatabase db;
    private MetinItem currentMetin;

    private View btnBookmarkSave;
    private ImageView imgBookmarkIcon;
    private TextView txtBookmarkStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        db = AppDatabase.getInstance(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSettings = findViewById(R.id.btnSettings);
        View cardSettingsPanel = findViewById(R.id.cardSettingsPanel);

        TextView txtBaslik = findViewById(R.id.baslık);
        TextView txtAciklama = findViewById(R.id.txtMainContent);
        EditText etPersonalNote = findViewById(R.id.etPersonalNote);

        // Kaydet Butonu Bileşenleri
        btnBookmarkSave = findViewById(R.id.btnBookmarkSave);
        imgBookmarkIcon = findViewById(R.id.imgBookmarkIcon);
        txtBookmarkStatus = findViewById(R.id.txtBookmarkStatus);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

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

        // Veri tabanından yükle ve durumları ayarla
        checkAndLoadDatabase(title, description, etPersonalNote);

        // Canlı Not Güncelleme
        if (etPersonalNote != null) {
            etPersonalNote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentMetin != null) {
                        currentMetin.setPersonalNote(s.toString());
                        new Thread(() -> db.metinDao().update(currentMetin)).start();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Kaydet Butonu Tıklama Olayı
        if (btnBookmarkSave != null) {
            btnBookmarkSave.setOnClickListener(v -> toggleSaveState());
        }
    }

    private void checkAndLoadDatabase(String title, String description, EditText etPersonalNote) {
        new Thread(() -> {
            List<MetinItem> list = db.metinDao().getAllMetinler();
            if (list.isEmpty()) {
                currentMetin = new MetinItem(title, description, "", false);
                db.metinDao().insert(currentMetin);
                list = db.metinDao().getAllMetinler();
                if (!list.isEmpty()) {
                    currentMetin = list.get(0);
                }
            } else {
                currentMetin = list.get(0);
            }

            runOnUiThread(() -> {
                if (currentMetin != null) {
                    if (etPersonalNote != null && currentMetin.getPersonalNote() != null) {
                        etPersonalNote.setText(currentMetin.getPersonalNote());
                    }
                    updateBookmarkUI(currentMetin.isSaved());
                }
            });
        }).start();
    }

    private void toggleSaveState() {
        if (currentMetin == null) return;

        boolean newSaveState = !currentMetin.isSaved();
        currentMetin.setSaved(newSaveState);

        new Thread(() -> {
            db.metinDao().update(currentMetin);

            runOnUiThread(() -> {
                updateBookmarkUI(newSaveState);
                String msg = newSaveState ? "Metin kaydedilenlere eklendi" : "Metin kaydedilenlerden çıkarıldı";
                Toast.makeText(kisisel_metin_okuma_sayfa.this, msg, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void updateBookmarkUI(boolean isSaved) {
        if (txtBookmarkStatus != null) {
            txtBookmarkStatus.setText(isSaved ? "Kaydedildi" : "Kaydet");
        }
        if (imgBookmarkIcon != null) {
            imgBookmarkIcon.setImageResource(isSaved ? R.drawable.ic_bookmark_save : R.drawable.ic_bookmark);
        }
    }
}