package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.kisisel_metin_okuma_sayfa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KisiselMetinlerimActivity extends AppCompatActivity {

    private static final String JSON_FILE_NAME = "kisisel_metinler.json";
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Locale TR_LOCALE = new Locale("tr", "TR");

    private TextView btnAll;
    private TextView btnPratikHayat;
    private TextView btnDisSeyler;
    private TextView btnZihinselMekanizma;
    private final List<TextView> categoryButtons = new ArrayList<>();

    private kaydedilenler_adapter adapter;
    private final List<kaydedilenler> masterList = new ArrayList<>();
    private final List<kaydedilenler> displayList = new ArrayList<>();
    private String currentSelectedCategory = "TÜMÜ";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ana_sayfa_kisisel_metinlerim);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromRoomDatabase();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAll = findViewById(R.id.tumu);
        btnPratikHayat = findViewById(R.id.kategori_2);
        btnDisSeyler = findViewById(R.id.kategori_1);
        btnZihinselMekanizma = findViewById(R.id.kategori_3);

        categoryButtons.clear();
        if (btnAll != null) categoryButtons.add(btnAll);
        if (btnPratikHayat != null) categoryButtons.add(btnPratikHayat);
        if (btnDisSeyler != null) categoryButtons.add(btnDisSeyler);
        if (btnZihinselMekanizma != null) categoryButtons.add(btnZihinselMekanizma);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPersonalTexts);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new kaydedilenler_adapter(this, displayList);
            adapter.setOnItemClickListener(item -> {
                if (item == null) return;
                Intent intent = new Intent(KisiselMetinlerimActivity.this, kisisel_metin_okuma_sayfa.class);
                intent.putExtra("TITLE", item.getTitle());
                intent.putExtra("CONTENT", item.getContent());
                intent.putExtra("DESCRIPTION", item.getDescription());
                intent.putExtra("READ_TIME", item.getAddedTime());
                intent.putExtra("CATEGORY", item.getCategory());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (btnAll != null) {
            btnAll.setOnClickListener(v -> filterCategory("TÜMÜ", btnAll));
        }
        if (btnPratikHayat != null) {
            btnPratikHayat.setOnClickListener(v -> filterCategory("Pratik Hayat İçin Fayda", btnPratikHayat));
        }
        if (btnDisSeyler != null) {
            btnDisSeyler.setOnClickListener(v -> filterCategory("Dış Şeylere Karşı Savunma", btnDisSeyler));
        }
        if (btnZihinselMekanizma != null) {
            btnZihinselMekanizma.setOnClickListener(v -> filterCategory("Zihinsel Mekanizma & Mimari", btnZihinselMekanizma));
        }
    }

    private void setupSearchListener() {
        EditText searchEditText = findViewById(R.id.searchBar);
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = (s != null) ? s.toString() : "";
                    filterCategory(currentSelectedCategory, getButtonForCategory(currentSelectedCategory));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadDataFromRoomDatabase() {
        DB_EXECUTOR.execute(() -> {
            List<kaydedilenler> rawList = parseRawListFromAsset();
            syncSavedStatesWithDb(rawList);

            runOnUiThread(() -> {
                masterList.clear();
                masterList.addAll(rawList);
                filterCategory(currentSelectedCategory, getButtonForCategory(currentSelectedCategory));
            });
        });
    }

    private List<kaydedilenler> parseRawListFromAsset() {
        List<kaydedilenler> rawList = new ArrayList<>();
        String jsonString = loadJSONFromAsset();
        if (jsonString != null && !jsonString.trim().isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<kaydedilenler>>() {}.getType();
                List<kaydedilenler> parsed = gson.fromJson(jsonString, listType);
                if (parsed != null) {
                    rawList.addAll(parsed);
                }
            } catch (Exception ignored) {}
        }
        return rawList;
    }

    private void syncSavedStatesWithDb(List<kaydedilenler> rawList) {
        AppDatabase db = AppDatabase.getInstance(KisiselMetinlerimActivity.this);
        if (db == null || rawList == null || rawList.isEmpty()) return;

        List<MetinItem> dbMetinler = db.metinDao().getAllMetinler();
        if (dbMetinler != null && !dbMetinler.isEmpty()) {
            for (kaydedilenler item : rawList) {
                if (item == null || item.getTitle() == null) continue;
                String cleanItemTitle = item.getTitle().replaceAll("\\s+", "").toLowerCase(TR_LOCALE);

                for (MetinItem dbItem : dbMetinler) {
                    if (dbItem != null && dbItem.getTitle() != null) {
                        String cleanDbTitle = dbItem.getTitle().replaceAll("\\s+", "").toLowerCase(TR_LOCALE);
                        if (cleanDbTitle.equals(cleanItemTitle)) {
                            item.setSaved(dbItem.isSaved());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void filterCategory(String categoryName, TextView selectedButton) {
        currentSelectedCategory = categoryName;

        for (TextView btn : categoryButtons) {
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.bg_search_bar);
                btn.setTextColor(Color.parseColor("#CCCCCC"));
            }
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_black_icon_box);
            selectedButton.setTextColor(Color.WHITE);
        }

        List<kaydedilenler> filtered = new ArrayList<>();
        String cleanQuery = currentSearchQuery.toLowerCase(TR_LOCALE).trim();

        for (kaydedilenler item : masterList) {
            if (item == null) continue;

            boolean matchesCategory = isCategoryMatching(categoryName, item);
            boolean matchesSearch = isSearchMatching(cleanQuery, item);

            if (matchesCategory && matchesSearch) {
                filtered.add(item);
            }
        }

        displayList.clear();
        displayList.addAll(filtered);

        if (adapter != null) {
            adapter.filterList(new ArrayList<>(displayList));
        }
    }

    private boolean isCategoryMatching(String categoryName, kaydedilenler item) {
        if (categoryName.equalsIgnoreCase("TÜMÜ")) {
            return true;
        }
        if (item.getCategory() != null) {
            String itemCategory = item.getCategory().toLowerCase(TR_LOCALE).trim();
            String targetCategory = categoryName.toLowerCase(TR_LOCALE).trim();
            return itemCategory.contains(targetCategory) || targetCategory.contains(itemCategory);
        }
        return true;
    }

    private boolean isSearchMatching(String cleanQuery, kaydedilenler item) {
        if (cleanQuery.isEmpty()) {
            return true;
        }
        String title = item.getTitle() != null ? item.getTitle().toLowerCase(TR_LOCALE) : "";
        String content = item.getContent() != null ? item.getContent().toLowerCase(TR_LOCALE) : "";
        String description = item.getDescription() != null ? item.getDescription().toLowerCase(TR_LOCALE) : "";

        return title.contains(cleanQuery) || content.contains(cleanQuery) || description.contains(cleanQuery);
    }

    private TextView getButtonForCategory(String categoryName) {
        switch (categoryName) {
            case "Pratik Hayat İçin Fayda": return btnPratikHayat;
            case "Dış Şeylere Karşı Savunma": return btnDisSeyler;
            case "Zihinsel Mekanizma & Mimari": return btnZihinselMekanizma;
            case "TÜMÜ": default: return btnAll;
        }
    }

    private String loadJSONFromAsset() {
        try (InputStream is = getAssets().open(JSON_FILE_NAME)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead > 0) {
                return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {}
        return null;
    }
}