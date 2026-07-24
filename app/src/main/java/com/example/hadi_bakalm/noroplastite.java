package com.example.hadi_bakalm;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class noroplastite extends AppCompatActivity {

    private ImageView btnBack;
    private FrameLayout btnDialogues, btnImportance;
    private View contentDialogues, contentImportance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroplastite);

        btnBack = findViewById(R.id.btnBack);

        // Butonlar (Tıklanacak Siyah Kutular)
        btnDialogues = findViewById(R.id.btnDialogues);
        btnImportance = findViewById(R.id.btnImportance);

        // İçerikler (Açılıp Kapanacak Paneller)
        contentDialogues = findViewById(R.id.contentDialogues);
        contentImportance = findViewById(R.id.contentImportance);

        // Geri Butonu
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Panel 1 Tıklama Mantığı
        if (btnDialogues != null && contentDialogues != null) {
            btnDialogues.setOnClickListener(v -> {
                if (contentDialogues.getVisibility() == View.VISIBLE) {
                    contentDialogues.setVisibility(View.GONE);
                } else {
                    contentDialogues.setVisibility(View.VISIBLE);
                }
            });
        }

        // Panel 2 Tıklama Mantığı
        if (btnImportance != null && contentImportance != null) {
            btnImportance.setOnClickListener(v -> {
                if (contentImportance.getVisibility() == View.VISIBLE) {
                    contentImportance.setVisibility(View.GONE);
                } else {
                    contentImportance.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}