package com.example.hadi_bakalm.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.data.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AyarlarActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AyarlarPrefs";
    public static final String KEY_THEME = "secilen_tema_pozisyon";

    private Button btnThemeLight, btnThemeDark, btnThemeSystem;
    private SharedPreferences sharedPreferences;
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ayarlar_sayfa);

        db = AppDatabase.getInstance(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        setupBottomNavigation();
        loadSavedTheme();
        setupClickListeners();
    }

    private void initViews() {
        btnThemeLight = findViewById(R.id.btnThemeLight);
        btnThemeDark = findViewById(R.id.btnThemeDark);
        btnThemeSystem = findViewById(R.id.btnThemeSystem);
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

    private void loadSavedTheme() {
        int savedThemePos = sharedPreferences.getInt(KEY_THEME, 0);
        updateThemeUI(savedThemePos);
    }

    private void setupClickListeners() {
        if (btnThemeLight != null) {
            btnThemeLight.setOnClickListener(v -> applyTheme(0));
        }
        if (btnThemeDark != null) {
            btnThemeDark.setOnClickListener(v -> applyTheme(1));
        }
        if (btnThemeSystem != null) {
            btnThemeSystem.setOnClickListener(v -> applyTheme(2));
        }

        View btnReset = findViewById(R.id.btnResetAll);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> showResetConfirmationDialog());
        }
    }

    private void applyTheme(int position) {
        int currentSaved = sharedPreferences.getInt(KEY_THEME, 0);

        if (currentSaved != position) {
            sharedPreferences.edit().putInt(KEY_THEME, position).apply();

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
        // Butonların aktif/pasif görsel durumlarını burada ayarlayabilirsin
        // Şimdilik temel mantık seçilen temayı kaydedip uyguluyor.
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
                Toast.makeText(AyarlarActivity.this, "Tüm veriler ve ayarlar başarıyla sıfırlandı.", Toast.LENGTH_SHORT).show();
            });
        });
    }
}