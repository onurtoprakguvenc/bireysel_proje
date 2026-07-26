package com.example.hadi_bakalm.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.hadi_bakalm.MainActivity;
import com.example.hadi_bakalm.R;

public class AyarlarActivity extends AppCompatActivity {

    private Spinner spinnerTheme;
    private Spinner spinnerAutoClear;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "AyarlarPrefs";
    private static final String KEY_THEME = "secilen_tema_pozisyon";
    private static final String KEY_AUTO_CLEAR = "secilen_otomatik_temizle_pozisyon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ayarlar_sayfa);
        NavigationHelper.setupBottomNavigation(this);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        setupBottomNavigation();
        setupSpinners();
        setupClickListeners();
    }


    private void initViews() {
        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerAutoClear = findViewById(R.id.spinnerAutoClear);
    }

    private void setupBottomNavigation() {
        LinearLayout navCategories = findViewById(R.id.navCategories);
        LinearLayout navSaved = findViewById(R.id.navSaved);
        LinearLayout navRecent = findViewById(R.id.navRecent);

        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                Intent intent = new Intent(AyarlarActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navSaved != null) {
            navSaved.setOnClickListener(v -> {
                Intent intent = new Intent(AyarlarActivity.this, kaydet_ana_sayfa.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navRecent != null) {
            navRecent.setOnClickListener(v -> {
                Intent intent = new Intent(AyarlarActivity.this, SonIncelemeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }
    }

    private void setupSpinners() {
        String[] themeOptions = {"Aydınlık", "Karanlık", "Sistem Varsayılanı"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                themeOptions
        );
        spinnerTheme.setAdapter(themeAdapter);

        int savedThemePos = sharedPreferences.getInt(KEY_THEME, 0);
        spinnerTheme.setSelection(savedThemePos);

        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        String[] clearOptions = {"Asla", "Haftalık", "Aylık"};
        ArrayAdapter<String> clearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                clearOptions
        );
        spinnerAutoClear.setAdapter(clearAdapter);

        int savedClearPos = sharedPreferences.getInt(KEY_AUTO_CLEAR, 0);
        spinnerAutoClear.setSelection(savedClearPos);

        spinnerAutoClear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt(KEY_AUTO_CLEAR, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }



    private void setupClickListeners() {
        findViewById(R.id.btnExportData).setOnClickListener(v -> {
            Toast.makeText(this, "Veriler dışa aktarma dosyası hazırlanıyor...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnResetAll).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tüm Verileri Sıfırla")
                    .setMessage("Kaydedilen tüm içerikler ve inceleme geçmişi kalıcı olarak silinecektir. Onaylıyor musunuz?")
                    .setPositiveButton("Sıfırla", (dialog, which) -> {
                        sharedPreferences.edit().clear().apply();
                        spinnerTheme.setSelection(0);
                        spinnerAutoClear.setSelection(0);

                        Toast.makeText(this, "Tüm veriler ve ayarlar başarıyla sıfırlandı.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Vazgeç", null)
                    .show();
        });
    }

}

