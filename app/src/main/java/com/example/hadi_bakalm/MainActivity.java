package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.ana_sayfa_adapter;
import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
import com.example.hadi_bakalm.model.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView btnMenu;
    private EditText searchBar;
    private RecyclerView recyclerViewCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Alt menü geçişlerini tek merkezden çalıştıran satır
        NavigationHelper.setupBottomNavigation(this);

        btnMenu = findViewById(R.id.btnMenu);
        searchBar = findViewById(R.id.searchBar);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);

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

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                // İleride: Seçenekler menüsü
            });
        }
    }
}