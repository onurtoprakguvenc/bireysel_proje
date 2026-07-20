package com.example.hadi_bakalm;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import android.content.Intent;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);

        // Hamburger menü butonu - drawer'ı aç
        findViewById(R.id.btnAcDrawer).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        // Drawer içindeki kategori öğeleri
        findViewById(R.id.drawerItemHerSey).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // ileride: tüm içeriği göster
        });


        findViewById(R.id.cardKavramlar).setOnClickListener(v -> {
            Intent intent = new Intent(this, kavramlar_sayfa.class);
            startActivity(intent);
        });

        findViewById(R.id.drawerItemKavramlar).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // ileride: kavramlar sayfasına git
        });

        findViewById(R.id.drawerItemKisiselMetinler).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // ileride: kişisel metinler sayfasına git
        });

        findViewById(R.id.drawerItemAyarlar).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // ileride: ayarlar sayfasına git
        });

        // Ana ekrandaki bölüm kartları
        findViewById(R.id.cardKavramlar).setOnClickListener(v -> {
            Intent intent = new Intent(this, kavramlar_sayfa.class);
            startActivity(intent);
        });

        findViewById(R.id.cardKisiselMetinler).setOnClickListener(v -> {
            // ileride: kişisel metinler sayfasına git
        });

        // Geri tuşuna basınca drawer açıksa önce onu kapat (yeni API)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
