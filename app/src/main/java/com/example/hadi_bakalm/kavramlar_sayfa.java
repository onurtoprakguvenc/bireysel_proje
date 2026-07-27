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
import com.example.hadi_bakalm.model.CategoryGroupModel;
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

        // 3. RecyclerView Ayarları (Yatay Yönlendirme Eklendi)
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
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
        List<CategoryGroupModel> anaListe = new ArrayList<>();

        // 1. Kategori: Nörobilim ve Beyin
        List<concept_kavram_model> norobilimKavramlari = new ArrayList<>();
        norobilimKavramlari.add(new concept_kavram_model("amigdala", "geçici"));
        norobilimKavramlari.add(new concept_kavram_model("pfc", "geçici"));
        norobilimKavramlari.add(new concept_kavram_model("Dopamin ve Dopamin Bazal Seviyesi", "geçici"));


        anaListe.add(new CategoryGroupModel("Nörobilim ve Beyin", norobilimKavramlari));

        // 2. Kategori örneği (isteğe bağlı yeni başlıklar eklenebilir)
    /*
    List<concept_kavram_model> psikolojiKavramlari = new ArrayList<>();
    psikolojiKavramlari.add(new concept_kavram_model("Bilişsel Çelişki", "geçici"));
    anaListe.add(new CategoryGroupModel("Bilişsel Psikoloji", psikolojiKavramlari));
    */

        if (recyclerView != null) {
            adapter = new concept_kavram_adapter(anaListe);
            recyclerView.setAdapter(adapter);
        }
    }
}
