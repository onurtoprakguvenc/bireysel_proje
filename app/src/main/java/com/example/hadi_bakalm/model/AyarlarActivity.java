package com.example.hadi_bakalm.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.data.AppDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AyarlarActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AyarlarPrefs";
    public static final String KEY_THEME = "secilen_tema_pozisyon";
    public static final String KEY_NOTIFICATION = "bildirim_durumu";

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    private TextView btnThemeLight, btnThemeDark, btnThemeSystem;
    private SwitchCompat switchNotification;

    private RelativeLayout btnExportData;
    private RelativeLayout btnResetAll;
    private RelativeLayout btnSupportDev;
    private RelativeLayout btnSendFeedback;

    private SharedPreferences sharedPreferences;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ayarlar_sayfa);

        db = AppDatabase.getInstance(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        setupBottomNavigation();
        loadSavedSettings();
        setupClickListeners();
    }

    private void initViews() {
        btnThemeLight = findViewById(R.id.btnThemeLight);
        btnThemeDark = findViewById(R.id.btnThemeDark);
        btnThemeSystem = findViewById(R.id.btnThemeSystem);

        switchNotification = findViewById(R.id.switchNotification);

        btnExportData = findViewById(R.id.btnExportData);
        btnResetAll = findViewById(R.id.btnResetAll);
        btnSupportDev = findViewById(R.id.btnSupportDev);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);
    }

    private void setupBottomNavigation() {
        LinearLayout navCategories = findViewById(R.id.navCategories);
        LinearLayout navSaved = findViewById(R.id.navSaved);
        LinearLayout navRecent = findViewById(R.id.navRecent);

        if (navCategories != null) {
            navCategories.setOnClickListener(v -> navigateTo(EskiMainActivity.class));
        }
        if (navSaved != null) {
            navSaved.setOnClickListener(v -> navigateTo(kaydet_ana_sayfa.class));
        }
        if (navRecent != null) {
            navRecent.setOnClickListener(v -> navigateTo(SonIncelemeActivity.class));
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(AyarlarActivity.this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void loadSavedSettings() {
        int savedThemePos = sharedPreferences.getInt(KEY_THEME, 0);
        updateThemeUI(savedThemePos);

        if (switchNotification != null) {
            boolean isNotifEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATION, false);
            switchNotification.setChecked(isNotifEnabled);
        }
    }

    private void setupClickListeners() {
        if (btnThemeLight != null) btnThemeLight.setOnClickListener(v -> applyTheme(0));
        if (btnThemeDark != null) btnThemeDark.setOnClickListener(v -> applyTheme(1));
        if (btnThemeSystem != null) btnThemeSystem.setOnClickListener(v -> applyTheme(2));

        if (switchNotification != null) {
            switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_NOTIFICATION, isChecked).apply();
                Toast.makeText(this, isChecked ? "Günlük hatırlatıcı açıldı (20:00)" : "Hatırlatıcı kapatıldı", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnExportData != null) {
            btnExportData.setOnClickListener(v -> exportUserDataAsJson());
        }

        if (btnSupportDev != null) {
            btnSupportDev.setOnClickListener(v -> {
                Intent intent = new Intent(AyarlarActivity.this, bagis_sayfa.class);
                startActivity(intent);
            });
        }

        if (btnSendFeedback != null) {
            btnSendFeedback.setOnClickListener(v -> sendFeedbackEmail());
        }

        if (btnResetAll != null) {
            btnResetAll.setOnClickListener(v -> showResetConfirmationDialog());
        }
    }

    private void applyTheme(int position) {
        int currentSaved = sharedPreferences.getInt(KEY_THEME, 0);
        if (currentSaved != position) {
            sharedPreferences.edit().putInt(KEY_THEME, position).apply();
            updateThemeUI(position);

            switch (position) {
                case 0:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case 1:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case 2:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            recreate();
        }
    }

    private void updateThemeUI(int position) {
        setSegmentedButtonState(btnThemeLight, position == 0);
        setSegmentedButtonState(btnThemeDark, position == 1);
        setSegmentedButtonState(btnThemeSystem, position == 2);
    }

    private void setSegmentedButtonState(TextView button, boolean isSelected) {
        if (button == null) return;
        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_black_pill);
            button.setTextColor(Color.WHITE);
            button.setTypeface(null, Typeface.BOLD);
        } else {
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(Color.parseColor("#64748B"));
            button.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void exportUserDataAsJson() {
        Toast.makeText(this, "Veriler hazırlanıyor...", Toast.LENGTH_SHORT).show();

        DB_EXECUTOR.execute(() -> {
            Map<String, Object> exportData = new HashMap<>();
            if (db != null) {
                exportData.put("kaydedilen_kavramlar", db.conceptDao_kavram().getAllConceptler());
                exportData.put("kaydedilen_metinler", db.metinDao().getAllMetinler());
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(exportData);

            runOnUiThread(() -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Notlar ve Kayıtlar Yedeği");
                shareIntent.putExtra(Intent.EXTRA_TEXT, jsonOutput);
                startActivity(Intent.createChooser(shareIntent, "Yedeği Paylaş / Kaydet"));
            });
        });
    }

    private void sendFeedbackEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"destek@hadibakalim.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hadi Bakalım - Geri Bildirim & Hata Raporu");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Cihaz Modeli: " + android.os.Build.MODEL + "\nUygulama Sürümü: v1.0.0\n\nGörüşleriniz:\n");

        try {
            startActivity(Intent.createChooser(emailIntent, "E-posta Uygulaması Seçin"));
        } catch (Exception e) {
            Toast.makeText(this, "E-posta gönderebilecek bir uygulama bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tüm Verileri Sıfırla")
                .setMessage("Kaydedilen tüm içerikler ve inceleme geçmişi kalıcı olarak silinecektir. Onaylıyor musunuz?")
                .setPositiveButton("Sıfırla", (dialog, which) -> resetAllData())
                .setNegativeButton("Vazgeç", null)
                .show();
    }

    private void resetAllData() {
        DB_EXECUTOR.execute(() -> {
            if (db != null) {
                List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
                if (allConcepts != null) {
                    for (ConceptItem_kavram concept : allConcepts) {
                        concept.setSaved(false);
                        concept.setLastViewedTime(0);
                        db.conceptDao_kavram().update(concept);
                    }
                }

                List<MetinItem> allMetinler = db.metinDao().getAllMetinler();
                if (allMetinler != null) {
                    for (MetinItem metin : allMetinler) {
                        metin.setSaved(false);
                        metin.setLastViewedTime(0);
                        db.metinDao().update(metin);
                    }
                }
            }

            runOnUiThread(() -> {
                sharedPreferences.edit().clear().apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                updateThemeUI(0);
                if (switchNotification != null) switchNotification.setChecked(false);
                Toast.makeText(AyarlarActivity.this, "Tüm veriler ve ayarlar başarıyla sıfırlandı.", Toast.LENGTH_SHORT).show();
            });
        });
    }
}