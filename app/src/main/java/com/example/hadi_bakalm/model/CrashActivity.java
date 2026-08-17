package com.example.hadi_bakalm.model;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.R;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        String rawErrorLog = getIntent().getStringExtra("EXTRA_ERROR_LOG");
        if (rawErrorLog == null || rawErrorLog.trim().isEmpty()) {
            rawErrorLog = "Hata detayı bulunamadı.";
        }

        TextView txtErrorDetails = findViewById(R.id.txtErrorDetails);
        Button btnToggleDetails = findViewById(R.id.btnToggleDetails);
        Button btnRestartApp = findViewById(R.id.btnRestartApp);

        String dynamicExplanation = buildDynamicExplanation(rawErrorLog);
        String fullDisplayText = dynamicExplanation + "\n\n--- [TEKNİK KOD DÖKÜMÜ] ---\n" + rawErrorLog;

        if (txtErrorDetails != null) {
            txtErrorDetails.setMovementMethod(new ScrollingMovementMethod());
            txtErrorDetails.setText(fullDisplayText);

            txtErrorDetails.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("Crash Log", fullDisplayText));
                    Toast.makeText(this, "Hata raporu panoya kopyalandı", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnToggleDetails != null && txtErrorDetails != null) {
            btnToggleDetails.setOnClickListener(v -> {
                int visibility = txtErrorDetails.getVisibility();
                txtErrorDetails.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        if (btnRestartApp != null) {
            btnRestartApp.setOnClickListener(v -> restartApplicationCompletely());
        }
    }

    /**
     * Uygulamayı işletim sistemi düzeyinde tamamen kapatıp sıfırdan başlatan mekanizma.
     */
    private void restartApplicationCompletely() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        // Mevcut hata sürecini (crash process) anında sonlandırıyoruz
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private String buildDynamicExplanation(String rawLog) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("DURUM ANALİZİ:\n");

        if (rawLog.contains("OutOfMemoryError")) {
            explanation.append("Bellek (RAM) yetersizliği yaşandı. Çok yüksek çözünürlüklü görseller veya yoğun işlem yükü belleği tüketmiş olabilir.");
        } else if (rawLog.contains("SQLiteException") || rawLog.contains("Room") || rawLog.contains("android.database")) {
            explanation.append("Veritabanı okuma/yazma işlemi sırasında depolama katmanında geçici bir çakışma yaşandı.");
        } else if (rawLog.contains("NullPointerException")) {
            explanation.append("Sistem o an ihtiyaç duyduğu kritik bir veri alanına (başlık, içerik veya kimlik numarası) ulaşamadığı için güvenlik kalkanı devreye girdi.");
        } else if (rawLog.contains("ActivityNotFoundException")) {
            explanation.append("Paylaşım veya e-posta işlemi için cihazınızda bu görevi yürütebilecek harici bir uygulama bulunamadı.");
        } else if (rawLog.contains("SecurityException")) {
            explanation.append("Galeri veya dosya erişimi sırasında işletim sistemi izin kısıtlamasına takıldı.");
        } else if (rawLog.contains("IndexOutOfBoundsException")) {
            explanation.append("Tablo veya liste seçiminde sınırların dışına çıkıldı.");
        } else {
            explanation.append("Arayüz bileşenleri ile sistem kaynakları arasında öngörülemeyen bir senkronizasyon kopması yaşandı.");
        }

        String location = extractLocation(rawLog);
        if (!location.isEmpty()) {
            explanation.append("\n\nKAYNAK:\n").append(location);
        }

        return explanation.toString();
    }

    private String extractLocation(String rawLog) {
        String packagePrefix = "com.example.hadi_bakalm";
        int index = rawLog.indexOf(packagePrefix);
        if (index != -1) {
            int endIndex = rawLog.indexOf(")", index);
            if (endIndex != -1) {
                return rawLog.substring(index, endIndex + 1);
            }
        }
        return "";
    }
}