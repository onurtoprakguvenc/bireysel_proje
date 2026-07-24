package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.ana_sayfa_adapter;
import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
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

        View topContainer = findViewById(R.id.topContainer);

        ViewCompat.setOnApplyWindowInsetsListener(topContainer, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top + 16,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

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
        btnMenu.setOnClickListener(v -> {
            // İleride: Seçenekler menüsü
        });

        // Alt Gezinme Çubuğu (BottomNavigationView) Tıklama Yönetimi
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_categories) {
                // Zaten kategoriler ekranındayız
                return true;
            } else if (itemId == R.id.nav_saved) {
                // Kaydedilenler sayfasına geçiş
                Intent intent = new Intent(MainActivity.this, kaydet_ana_sayfa.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_recent) {
                // İleride: Son İnceleme
                return true;
            } else if (itemId == R.id.nav_settings) {
                // İleride: Ayarlar
                return true;
            }

            return false;
        });
    }
}