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
import com.example.hadi_bakalm.data.ConceptRepository;
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

        // 3. RecyclerView Ayarları (Kategoriler Dikey Dizilir, Kartlar Yatay Kayar)
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
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

        // 1. JSON'dan verileri anaListe'ye doldurur
        loadConceptsFromJSON(anaListe);

        // 2. Doğru ve Tek Yetkili Adaptörü Bağlar
        if (recyclerView != null) {
            adapter = new concept_kavram_adapter(anaListe);
            recyclerView.setAdapter(adapter);
        }
    }

    private void loadConceptsFromJSON(List<CategoryGroupModel> anaListe) {
        String jsonString = loadJSONFromAsset("kavramlar.json");
        if (jsonString == null) return;

        try {
            // Gson Builder ile bilinmeyen alanları yoksayma garantisi
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<JsonConceptModel>>() {}.getType();
            List<JsonConceptModel> rawList = gson.fromJson(jsonString, listType);


            if (rawList != null && !rawList.isEmpty()) {
                java.util.Map<String, List<concept_kavram_model>> groupedMap = new java.util.LinkedHashMap<>();

                for (JsonConceptModel item : rawList) {
                    String category = (item.category != null && !item.category.trim().isEmpty())
                            ? item.category
                            : "Diğer Kavramlar";

                    if (!groupedMap.containsKey(category)) {
                        groupedMap.put(category, new ArrayList<>());
                    }

                    String title = item.title != null ? item.title : "";
                    String desc = item.description != null ? item.description : "";
                    String content = item.content != null ? item.content : "";

                    groupedMap.get(category).add(new concept_kavram_model(item.id, title, desc, content));
                }

                for (java.util.Map.Entry<String, List<concept_kavram_model>> entry : groupedMap.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        anaListe.add(new CategoryGroupModel(entry.getKey(), entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Temizlenmiş Kavram Helper Modeli (readTime Barındırmaz)
    private static class JsonConceptModel {
        public String id;
        public String title;
        public String category;
        public String description;
        public String content;
    }

    // Asset Dosyası Okuyucu
    private String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            java.io.InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}