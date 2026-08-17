package com.example.hadi_bakalm;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class kavramlar_sayfa extends AppCompatActivity {

    private static final String TAG = "KAVRAM_KONTROL";
    private static final String JSON_FILE_NAME = "kavramlar.json";
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
        new Thread(() -> {
            String jsonMetni = loadJSONFromAsset();
            List<CategoryGroupModel> yuklenenListe = loadConceptsFromJSON(jsonMetni);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                anaListe.clear();
                anaListe.addAll(yuklenenListe);

                if (adapter != null) {
                    adapter.updateData(anaListe);
                }
            });
        }).start();
    }

    private List<CategoryGroupModel> loadConceptsFromJSON(String jsonString) {
        List<CategoryGroupModel> sonucListesi = new ArrayList<>();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return sonucListesi;
        }

        try {
            JsonArray jsonArray = extractJsonArray(jsonString);

            if (jsonArray != null && !jsonArray.isEmpty()) {
                Map<String, List<concept_kavram_model>> groupedMap = new LinkedHashMap<>();

                for (JsonElement elem : jsonArray) {
                    if (!elem.isJsonObject()) continue;

                    JsonConceptModel item = GSON.fromJson(elem, JsonConceptModel.class);
                    if (item == null) continue;

                    String category = (item.category != null && !item.category.trim().isEmpty())
                            ? item.category
                            : "Genel Kavramlar";

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
            Log.e(TAG, "JSON Ayrıştırma Hatası: ", e);
        }

        return sonucListesi;
    }

    private JsonArray extractJsonArray(String jsonString) {
        JsonElement rootElement = JsonParser.parseString(jsonString);

        if (rootElement.isJsonArray()) {
            return rootElement.getAsJsonArray();
        } else if (rootElement.isJsonObject()) {
            JsonObject rootObj = rootElement.getAsJsonObject();
            if (rootObj.has("kavramlar")) {
                return rootObj.getAsJsonArray("kavramlar");
            } else if (rootObj.has("concepts")) {
                return rootObj.getAsJsonArray("concepts");
            }
        }
        return null;
    }

    private String loadJSONFromAsset() {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream is = getAssets().open(JSON_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception ex) {
            Log.e(TAG, "Asset okunamadı (" + JSON_FILE_NAME + "): ", ex);
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