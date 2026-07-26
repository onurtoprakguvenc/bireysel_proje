package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class kisisel_metin_okuma_sayfa extends AppCompatActivity {

    private float currentFontSizeSp = 16f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSettings = findViewById(R.id.btnSettings);
        View cardSettingsPanel = findViewById(R.id.cardSettingsPanel);

        TextView txtBaslik = findViewById(R.id.baslık);
        TextView txtAciklama = findViewById(R.id.txtMainContent);

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

        // Panel içi butonlar
        if (cardSettingsPanel != null) {
            TextView btnTextDecrease = cardSettingsPanel.findViewById(R.id.btnTextDecrease);
            TextView btnTextIncrease = cardSettingsPanel.findViewById(R.id.btnTextIncrease);
            TextView txtFontSizeIndicator = cardSettingsPanel.findViewById(R.id.txtFontSizeIndicator);

            if (btnTextDecrease != null && txtAciklama != null) {
                btnTextDecrease.setOnClickListener(v -> {
                    if (currentFontSizeSp > 12f) {
                        currentFontSizeSp -= 2f;
                        txtAciklama.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp);
                        if (txtFontSizeIndicator != null) {
                            txtFontSizeIndicator.setText((int) currentFontSizeSp + " sp");
                        }
                    }
                });
            }

            if (btnTextIncrease != null && txtAciklama != null) {
                btnTextIncrease.setOnClickListener(v -> {
                    if (currentFontSizeSp < 28f) {
                        currentFontSizeSp += 2f;
                        txtAciklama.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp);
                        if (txtFontSizeIndicator != null) {
                            txtFontSizeIndicator.setText((int) currentFontSizeSp + " sp");
                        }
                    }
                });
            }
        }

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("TITLE");
            String description = intent.getStringExtra("DESCRIPTION");

            if (txtBaslik != null && title != null) {
                txtBaslik.setText(title);
            }
            if (txtAciklama != null && description != null) {
                txtAciklama.setText(description);
            }
        }
    }
}