package com.example.hadi_bakalm;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.concept_kavram_adapter;
import com.example.hadi_bakalm.model.CategoryGroupModel;
import com.example.hadi_bakalm.model.concept_kavram_model;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class kavramlar_sayfa extends AppCompatActivity {

    private static final String TAG = "KavramlarSayfa";
    private static final String JSON_FILE_NAME = "kavramlar.json";
    private static final ExecutorService BG_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Gson GSON = new Gson();

    private concept_kavram_adapter adapter;
    private final List<CategoryGroupModel> anaListe = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kavramlar_sayfa);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMainCategories);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            adapter = new concept_kavram_adapter(anaListe);
            recyclerView.setAdapter(adapter);
        }

        loadConceptDataAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConceptDataAsync();
    }

    private void loadConceptDataAsync() {
        BG_EXECUTOR.execute(() -> {
            List<CategoryGroupModel> yuklenenListe = loadConceptsFromJSON();

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                anaListe.clear();
                anaListe.addAll(yuklenenListe);

                if (adapter != null) {
                    updateAdapter();
                }
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateAdapter() {
        adapter.notifyDataSetChanged();
    }

    private List<CategoryGroupModel> loadConceptsFromJSON() {
        List<CategoryGroupModel> sonucListesi = new ArrayList<>();
        String jsonString = loadJSONFromAsset();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return sonucListesi;
        }

        try {
            Type listType = new TypeToken<List<JsonConceptModel>>() {}.getType();
            List<JsonConceptModel> rawList = GSON.fromJson(jsonString, listType);

            if (rawList != null && !rawList.isEmpty()) {
                Map<String, List<concept_kavram_model>> groupedMap = new LinkedHashMap<>();

                for (JsonConceptModel item : rawList) {
                    if (item == null) continue;

                    String category = (item.category != null && !item.category.trim().isEmpty())
                            ? item.category
                            : "Diğer Kavramlar";

                    if (!groupedMap.containsKey(category)) {
                        groupedMap.put(category, new ArrayList<>());
                    }

                    String id = item.id != null ? item.id : "";
                    String title = item.title != null ? item.title : "";
                    String desc = item.description != null ? item.description : "";
                    String content = item.content != null ? item.content : "";

                    List<concept_kavram_model> list = groupedMap.get(category);
                    if (list != null) {
                        list.add(new concept_kavram_model(id, title, desc, content));
                    }
                }

                for (Map.Entry<String, List<concept_kavram_model>> entry : groupedMap.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                        sonucListesi.add(new CategoryGroupModel(entry.getKey(), entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON parse hatası", e);
        }

        return sonucListesi;
    }

    private String loadJSONFromAsset() {
        try (InputStream is = getAssets().open(JSON_FILE_NAME)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) return null;
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Log.e(TAG, "Asset okuma hatası: " + JSON_FILE_NAME, ex);
            return null;
        }
    }

    private static class JsonConceptModel {
        public String id;
        public String title;
        public String category;
        public String description;
        public String content;
    }
}