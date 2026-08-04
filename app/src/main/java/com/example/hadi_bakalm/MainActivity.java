package com.example.hadi_bakalm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import com.example.hadi_bakalm.model.bagis_sayfa;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.ana_sayfa_adapter;
import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
import com.example.hadi_bakalm.model.NavigationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView btnMenu;
    private EditText searchBar;
    private RecyclerView recyclerViewCategories;
    private FloatingActionButton btnSupportDonate;
    private ana_sayfa_adapter adapter;
    private List<String> kategoriListesi;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_DISCLAIMER_ACCEPTED = "is_disclaimer_accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ekran çizilmeden (setContentView) ÖNCE temayı yükle
        applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationHelper.setupBottomNavigation(this);

        initViews();
        setupRecyclerView();
        setupSearch();

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                // İleride: Seçenekler menüsü
            });
        }

        // Bağış Butonu Tıklama Olayı (bağış sayfasına yönlendirme)
        if (btnSupportDonate != null) {
            btnSupportDonate.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, bagis_sayfa.class);
                startActivity(intent);
            });
        }

        // Sadece daha önce kabul edilmediyse diyalogu gösterir
        checkAndShowDisclaimer();
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("AyarlarPrefs", Context.MODE_PRIVATE);
        int position = prefs.getInt("secilen_tema_pozisyon", 0);

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
    }

    private void initViews() {
        btnMenu = findViewById(R.id.btnMenu);
        searchBar = findViewById(R.id.searchBar);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        btnSupportDonate = findViewById(R.id.btnSupportDonate);
    }

    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        kategoriListesi = new ArrayList<>();
        kategoriListesi.add("Kavramlar");
        kategoriListesi.add("Kişisel Metinlerim");

        adapter = new ana_sayfa_adapter(kategoriListesi, new ana_sayfa_adapter.OnItemClickListener() {
            @Override
            public void onItemClick(String kategoriAdi) {
                if (kategoriAdi.equals("Kavramlar")) {
                    Intent intent = new Intent(MainActivity.this, kavramlar_sayfa.class);
                    startActivity(intent);
                } else if (kategoriAdi.equals("Kişisel Metinlerim")) {
                    Intent intent = new Intent(MainActivity.this, KisiselMetinlerimActivity.class);
                    startActivity(intent);
                }
            }
        });

        recyclerViewCategories.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchBar == null) return;

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filter(String text) {
        java.util.Locale trLocale = new java.util.Locale("tr", "TR");

        String query = text != null ? text.toLowerCase(trLocale).trim() : "";
        List<String> filteredList = new ArrayList<>();

        for (String item : kategoriListesi) {
            if (item.toLowerCase(trLocale).contains(query)) {
                filteredList.add(item);
            }
        }

        if (adapter != null) {
            adapter.filterList(filteredList);
        }
    }

    private void checkAndShowDisclaimer() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isAccepted = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false);

        if (!isAccepted) {
            showExpertDisclaimerDialog();
        }
    }

    private void showExpertDisclaimerDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.diyalog_uzman_uyari, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();

        // Dışarıya tıklanarak kapatılmasını engeller (Zorunlu görünüm)
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Arka planı şeffaf yaparak kendi özel oval tasarımımızın görünmesini sağlar
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView btnAccept = dialogView.findViewById(R.id.btnAcceptDisclaimer);
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                // Kullanıcı onayladığında tercihi hafızaya kaydet
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, true).apply();

                dialog.dismiss();
            });
        }

        dialog.show();
    }
}