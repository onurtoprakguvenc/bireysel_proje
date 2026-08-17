package com.example.hadi_bakalm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.ana_sayfa_adapter;
import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
import com.example.hadi_bakalm.model.MainActivity;
import com.example.hadi_bakalm.model.NavigationHelper;
import com.example.hadi_bakalm.model.bagis_sayfa;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EskiMainActivity extends AppCompatActivity {

    private static final Locale TR_LOCALE = new Locale("tr", "TR");
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_DISCLAIMER_ACCEPTED = "is_disclaimer_accepted";

    private EditText searchBar;
    private ana_sayfa_adapter adapter;
    private final List<String> kategoriListesi = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationHelper.setupBottomNavigation(this);

        initViews();
        setupRecyclerView();
        setupSearch();
        checkAndShowDisclaimer();
    }

    private void initViews() {
        ImageView btnBackToNotes = findViewById(R.id.btnBackToNotes);
        searchBar = findViewById(R.id.searchBar);
        FloatingActionButton btnSupportDonate = findViewById(R.id.btnSupportDonate);

        if (btnBackToNotes != null) {
            btnBackToNotes.setOnClickListener(v -> {
                Intent intent = new Intent(EskiMainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnSupportDonate != null) {
            btnSupportDonate.setOnClickListener(v -> {
                Intent intent = new Intent(EskiMainActivity.this, bagis_sayfa.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        if (recyclerViewCategories == null) return;

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        kategoriListesi.clear();
        kategoriListesi.add("Kavramlar");
        kategoriListesi.add("Kişisel Metinlerim");

        adapter = new ana_sayfa_adapter(kategoriListesi, kategoriAdi -> {
            if (isFinishing() || isDestroyed()) return;

            if ("Kavramlar".equals(kategoriAdi)) {
                startActivity(new Intent(EskiMainActivity.this, kavramlar_sayfa.class));
            } else if ("Kişisel Metinlerim".equals(kategoriAdi)) {
                startActivity(new Intent(EskiMainActivity.this, KisiselMetinlerimActivity.class));
            }
        });

        recyclerViewCategories.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchBar == null) return;

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        String query = text != null ? text.toLowerCase(TR_LOCALE).trim() : "";
        List<String> filteredList = new ArrayList<>();

        for (String item : kategoriListesi) {
            if (item != null && item.toLowerCase(TR_LOCALE).contains(query)) {
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
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.diyalog_uzman_uyari, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnAccept = dialogView.findViewById(R.id.btnAcceptDisclaimer);
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, true).apply();
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}