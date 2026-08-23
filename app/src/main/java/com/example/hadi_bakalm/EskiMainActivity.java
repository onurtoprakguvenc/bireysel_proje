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

import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
import com.example.hadi_bakalm.model.MainActivity;
import com.example.hadi_bakalm.model.NavigationHelper;
import com.example.hadi_bakalm.model.bagis_sayfa;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class EskiMainActivity extends AppCompatActivity {

    private static final Locale TR_LOCALE = new Locale("tr", "TR");
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_DISCLAIMER_ACCEPTED = "is_disclaimer_accepted";

    private EditText searchBar;
    private MaterialCardView cardKavramlar;
    private MaterialCardView cardKisiselMetinler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationHelper.setupBottomNavigation(this);

        initViews();
        setupCategoryCards();
        setupSearch();
        checkAndShowDisclaimer();
    }

    private void initViews() {
        ImageView btnBackToNotes = findViewById(R.id.btnBackToNotes);
        searchBar = findViewById(R.id.searchBar);
        FloatingActionButton btnSupportDonate = findViewById(R.id.btnSupportDonate);

        cardKavramlar = findViewById(R.id.cardKavramlar);
        cardKisiselMetinler = findViewById(R.id.cardKisiselMetinler);

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

    private void setupCategoryCards() {
        if (cardKavramlar != null) {
            cardKavramlar.setOnClickListener(v -> {
                if (isFinishing() || isDestroyed()) return;
                startActivity(new Intent(EskiMainActivity.this, kavramlar_sayfa.class));
            });
        }

        if (cardKisiselMetinler != null) {
            cardKisiselMetinler.setOnClickListener(v -> {
                if (isFinishing() || isDestroyed()) return;
                startActivity(new Intent(EskiMainActivity.this, KisiselMetinlerimActivity.class));
            });
        }
    }

    private void setupSearch() {
        if (searchBar == null) return;

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCards(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCards(String query) {
        String cleanQuery = query != null ? query.toLowerCase(TR_LOCALE).trim() : "";

        if (cardKavramlar != null) {
            boolean matchesKavramlar = cleanQuery.isEmpty()
                    || "kavramlar".contains(cleanQuery)
                    || "zihinsel modeller tanımlar çalışma kuralları".toLowerCase(TR_LOCALE).contains(cleanQuery);
            cardKavramlar.setVisibility(matchesKavramlar ? View.VISIBLE : View.GONE);
        }

        if (cardKisiselMetinler != null) {
            boolean matchesMetinler = cleanQuery.isEmpty()
                    || "kişisel metinlerim".contains(cleanQuery)
                    || "deneyimler çıkarımlar manifestolar gözlemler".toLowerCase(TR_LOCALE).contains(cleanQuery);
            cardKisiselMetinler.setVisibility(matchesMetinler ? View.VISIBLE : View.GONE);
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