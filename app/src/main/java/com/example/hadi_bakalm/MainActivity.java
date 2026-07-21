package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ImageView btnMenu;
    private EditText searchBar;
    private RecyclerView recyclerViewCategories;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Yeni XML bileşenlerinin ID bağlantıları
        btnMenu = findViewById(R.id.btnMenu);
        searchBar = findViewById(R.id.searchBar);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // RecyclerView Düzeni
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        // Üst sağ 3 nokta menü butonu
        btnMenu.setOnClickListener(v -> {
            // İleride: Seçenekler menüsü veya pop-up açılacak
        });

        // Alt Gezinme Çubuğu (BottomNavigationView) Tıklama Yönetimi
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_categories) {
                // Zaten kategoriler ekranındayız
                return true;
            } else if (itemId == R.id.nav_saved) {
                // İleride: Kaydedilenler sayfasına git
                return true;
            } else if (itemId == R.id.nav_recent) {
                // İleride: Son İnceleme sayfasına git
                return true;
            } else if (itemId == R.id.nav_settings) {
                // İleride: Ayarlar sayfasına git
                return true;
            }

            return false;
        });

        /*
           NOT: "kavramlar_sayfa" Activity yönlendirmesi artık Adapter içinde yapılacaktır.
           RecyclerView Adapter'ı bağlandığında, tıklanan öğenin başlığına göre
           ("kavramlar" ise kavramlar_sayfa.class) Intent başlatılacaktır.
        */
    }
}