package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.ana_sayfa_adapter;
import com.example.hadi_bakalm.model.AyarlarActivity;
import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
import com.example.hadi_bakalm.model.SonIncelemeActivity;
import com.example.hadi_bakalm.model.kaydet_ana_sayfa;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView btnMenu;
    private EditText searchBar;
    private RecyclerView recyclerViewCategories;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML bileşenlerinin ID bağlantıları
        btnMenu = findViewById(R.id.btnMenu);
        searchBar = findViewById(R.id.searchBar);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // RecyclerView Düzeni
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        List<String> kategoriListesi = new ArrayList<>();
        kategoriListesi.add("Kavramlar");
        kategoriListesi.add("Kişisel Metinlerim");

        ana_sayfa_adapter adapter = new ana_sayfa_adapter(kategoriListesi, new ana_sayfa_adapter.OnItemClickListener() {
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

        // Üst sağ 3 nokta menü butonu
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                // İleride: Seçenekler menüsü
            });
        }


        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, AyarlarActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_categories) { // Veya ana sayfa ID'si
                // Zaten MainActivity üzerindesin, tekrar startActivity YAPMA!
                return true;
            }
            return false;
        });

        // Alt Gezinme Çubuğu (BottomNavigationView) Tıklama Yönetimi
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_categories) {
                return true;
            } else if (itemId == R.id.nav_saved) {
                Intent intent = new Intent(MainActivity.this, kaydet_ana_sayfa.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_recent) {
                Intent intent = new Intent(MainActivity.this, SonIncelemeActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_categories);
        }
    }
}

