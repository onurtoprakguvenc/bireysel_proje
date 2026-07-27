package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.concept_kavram_adapter;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.concept_kavram_model;

import java.util.ArrayList;
import java.util.List;

public class kavramlar_sayfa extends AppCompatActivity {

    private RecyclerView recyclerView;
    private concept_kavram_adapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kavramlar_sayfa);

        // 1. Veri Tabanı Bağlantısı
        db = AppDatabase.getInstance(this);

        // 2. Arayüz Elemanları
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewMainCategories);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. RecyclerView Ayarları
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // 4. Verileri Yükle
        loadConceptList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Detay sayfasından geri dönüldüğünde (örn: kaydedildi durumu değiştiyse) listeyi tazele
        loadConceptList();
    }

    private void loadConceptList() {
        List<concept_kavram_model> liste = new ArrayList<>();

        // Kategori Başlığı
        liste.add(new concept_kavram_model("Nörobilim ve Beyin"));

        // Kavram Kartları
        liste.add(new concept_kavram_model(
                "amigdala",
                "geçici"
        ));

        liste.add(new concept_kavram_model(
                "pfc",
                "geçici"
        ));

        liste.add(new concept_kavram_model(
                "Dopamin ve Dopamin Bazal Seviyesi",
                "geçici"
        ));

        if (recyclerView != null) {
            adapter = new concept_kavram_adapter(liste);
            recyclerView.setAdapter(adapter);
        }
    }
}