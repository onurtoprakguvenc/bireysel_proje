package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class noroplastite extends AppCompatActivity {

    private ImageView btnBack;
    private FrameLayout btnDialogues, btnImportance;
    private View contentDialogues, contentImportance;

    // Panoya Kopyalama Yardımcı Metodu
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " panoya kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    // Metin Paylaşma Yardımcı Metodu (Sistem Paylaşım Menüsünü Açar)
    private void shareText(String title, String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Şununla paylaş:"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroplastite);


// 1. Kart (Kavram Açıklaması) için Butonlar
        TextView btnCopy1 = findViewById(R.id.btnCopy1);
        TextView btnShare1 = findViewById(R.id.btnShare1);
        TextView txtConceptDescription = findViewById(R.id.txtConceptDescription);

// 2. Kart (Kişisel Not) için Butonlar
        TextView btnCopy2 = findViewById(R.id.btnCopy2);
        TextView btnShare2 = findViewById(R.id.btnShare2);
        TextView txtPersonalNote = findViewById(R.id.txtPersonalNote);

// --- 2. Tıklama Olaylarını (Listeners) Tanımlama ---

// 1. Kart Kopyala
        if (btnCopy1 != null && txtConceptDescription != null) {
            btnCopy1.setOnClickListener(v -> {
                copyToClipboard("Kavram Açıklaması", txtConceptDescription.getText().toString());
            });
        }

// 1. Kart Paylaş
        if (btnShare1 != null && txtConceptDescription != null) {
            btnShare1.setOnClickListener(v -> {
                shareText("Kavram Açıklaması", txtConceptDescription.getText().toString());
            });
        }

// 2. Kart Kopyala
        if (btnCopy2 != null && txtPersonalNote != null) {
            btnCopy2.setOnClickListener(v -> {
                copyToClipboard("Kişisel Not", txtPersonalNote.getText().toString());
            });
        }

// 2. Kart Paylaş
        if (btnShare2 != null && txtPersonalNote != null) {
            btnShare2.setOnClickListener(v -> {
                shareText("Kişisel Not", txtPersonalNote.getText().toString());
            });
        }

        // --- 1. Kopyala ve Paylaş Butonlarını Bağlama ---
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