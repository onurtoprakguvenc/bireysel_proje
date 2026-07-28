package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
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

    // Arayüz Elemanları
    private ImageView btnBack, btnSettings;
    private TextView txtBaslik, txtAciklama, txtReadTime;
    private EditText etPersonalNote;
    private TextView btnCopyMainText, btnShareMainText, btnCopyNote;

    // Ayar Paneli İçindeki Elemanlar
    private View cardSettingsPanel;
    private TextView btnThemeAydinlik, btnThemeSarimsi, btnThemeKaranlik;
    private TextView btnTextDecrease, btnTextIncrease, txtFontSizeIndicator;

    private SharedPreferences sharedPreferences;
    private int currentFontSize = 16;

    private void copyToClipboard(String label, String text) {
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(this, "Kopyalanacak metin bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " panoya kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareText(String title, String text) {
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(this, "Paylaşılacak metin bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Şununla paylaş:"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        db = AppDatabase.getInstance(this);

        sharedPreferences = getSharedPreferences("ReaderSettings", Context.MODE_PRIVATE);
        currentFontSize = sharedPreferences.getInt("READING_FONT_SIZE", 16);

        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        cardSettingsPanel = findViewById(R.id.cardSettingsPanel);

        txtBaslik = findViewById(R.id.baslık);
        txtAciklama = findViewById(R.id.txtMainContent);
        txtReadTime = findViewById(R.id.txtReadTime);
        etPersonalNote = findViewById(R.id.etPersonalNote);

        btnCopyMainText = findViewById(R.id.btnCopyMainText);
        btnShareMainText = findViewById(R.id.btnShareMainText);
        btnCopyNote = findViewById(R.id.btnCopyNote);

        btnBookmarkSave = findViewById(R.id.btnBookmarkSave);
        imgBookmarkIcon = findViewById(R.id.imgBookmarkIcon);
        txtBookmarkStatus = findViewById(R.id.txtBookmarkStatus);

        if (cardSettingsPanel != null) {
            btnThemeAydinlik = cardSettingsPanel.findViewById(R.id.btnThemeAydinlik);
            btnThemeSarimsi = cardSettingsPanel.findViewById(R.id.btnThemeSarimsi);
            btnThemeKaranlik = cardSettingsPanel.findViewById(R.id.btnThemeKaranlik);

            btnTextDecrease = cardSettingsPanel.findViewById(R.id.btnTextDecrease);
            btnTextIncrease = cardSettingsPanel.findViewById(R.id.btnTextIncrease);
            txtFontSizeIndicator = cardSettingsPanel.findViewById(R.id.txtFontSizeIndicator);

            cardSettingsPanel.setOnClickListener(v -> {});
        }

        applyFontSize(currentFontSize);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSettings != null && cardSettingsPanel != null) {
            btnSettings.setOnClickListener(v -> {
                int currentVisibility = cardSettingsPanel.getVisibility();
                cardSettingsPanel.setVisibility(currentVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        setupSettingsPanelListeners();

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

        if (btnCopyMainText != null && txtAciklama != null) {
            btnCopyMainText.setOnClickListener(v -> copyToClipboard("Metin", txtAciklama.getText().toString()));
        }

        if (btnShareMainText != null && txtAciklama != null) {
            String finalTitle = title;
            btnShareMainText.setOnClickListener(v -> shareText(finalTitle, txtAciklama.getText().toString()));
        }

        if (btnCopyNote != null && etPersonalNote != null) {
            btnCopyNote.setOnClickListener(v -> copyToClipboard("Kişisel Not", etPersonalNote.getText().toString()));
        }

        checkAndLoadDatabase(title, description, etPersonalNote);

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

        if (btnBookmarkSave != null) {
            btnBookmarkSave.setOnClickListener(v -> toggleSaveState());
        }
    }

    private void setupSettingsPanelListeners() {
        if (btnTextDecrease != null) {
            btnTextDecrease.setOnClickListener(v -> {
                if (currentFontSize > 12) {
                    currentFontSize--;
                    applyFontSize(currentFontSize);
                    saveFontSize(currentFontSize);
                }
            });
        }

        if (btnTextIncrease != null) {
            btnTextIncrease.setOnClickListener(v -> {
                if (currentFontSize < 24) {
                    currentFontSize++;
                    applyFontSize(currentFontSize);
                    saveFontSize(currentFontSize);
                }
            });
        }
    }

    private void applyFontSize(int sizeSp) {
        if (txtAciklama != null) txtAciklama.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (etPersonalNote != null) etPersonalNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (txtFontSizeIndicator != null) txtFontSizeIndicator.setText(sizeSp + "sp");
    }

    private void saveFontSize(int sizeSp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("READING_FONT_SIZE", sizeSp);
        editor.apply();
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