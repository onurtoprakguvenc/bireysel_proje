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
import com.example.hadi_bakalm.model.MetinItem;
import com.example.hadi_bakalm.model.kaydedilenler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class KisiselMetinlerimActivity extends AppCompatActivity {

    private TextView btnAll, btnPratikHayat, btnDisSeyler, btnZihinselMekanizma;
    private List<TextView> categoryButtons;

    private RecyclerView recyclerView;
    private kaydedilenler_adapter adapter;
    private List<kaydedilenler> masterList;
    private List<kaydedilenler> displayList;
    private String currentSelectedCategory = "TÜMÜ";

    // Arama Çubuğu Tanımlaması
    private EditText searchEditText;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ana_sayfa_kisisel_metinlerim);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Kategori Butonları
        btnAll = findViewById(R.id.tumu);
        btnPratikHayat = findViewById(R.id.kategori_2);
        btnDisSeyler = findViewById(R.id.kategori_1);
        btnZihinselMekanizma = findViewById(R.id.kategori_3);

        categoryButtons = new ArrayList<>();
        if (btnAll != null) categoryButtons.add(btnAll);
        if (btnPratikHayat != null) categoryButtons.add(btnPratikHayat);
        if (btnDisSeyler != null) categoryButtons.add(btnDisSeyler);
        if (btnZihinselMekanizma != null) categoryButtons.add(btnZihinselMekanizma);

        recyclerView = findViewById(R.id.recyclerViewPersonalTexts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        displayList = new ArrayList<>();

        // Arama Çubuğu Bağlantısı ve Dinleyicisi
        searchEditText = findViewById(R.id.searchBar);
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    filterCategory(currentSelectedCategory, getButtonForCategory(currentSelectedCategory));
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // Tıklama Olayları
        if (btnAll != null) btnAll.setOnClickListener(v -> filterCategory("TÜMÜ", btnAll));
        if (btnPratikHayat != null)
            btnPratikHayat.setOnClickListener(v -> filterCategory("Pratik Hayat İçin Fayda", btnPratikHayat));
        if (btnDisSeyler != null)
            btnDisSeyler.setOnClickListener(v -> filterCategory("Dış Şeylere Karşı Savunma", btnDisSeyler));
        if (btnZihinselMekanizma != null)
            btnZihinselMekanizma.setOnClickListener(v -> filterCategory("Zihinsel Mekanizma & Mimari", btnZihinselMekanizma));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromRoomDatabase();
    }

    private void loadDataFromRoomDatabase() {
        new Thread(() -> {
            String jsonString = loadJSONFromAsset("kisisel_metinler.json");
            List<kaydedilenler> rawList = new ArrayList<>();

            if (jsonString != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<kaydedilenler>>() {
                }.getType();
                rawList = gson.fromJson(jsonString, listType);
            }

            // DOĞRUDAN KİŞİSEL METİNLER DAO'SU İLE EŞLEŞTİRME Yapılıyor:
            AppDatabase db = AppDatabase.getInstance(KisiselMetinlerimActivity.this);
            List<MetinItem> dbMetinler = db.metinDao().getAllMetinler();

            if (dbMetinler != null && !dbMetinler.isEmpty() && rawList != null) {
                for (kaydedilenler item : rawList) {
                    if (item.getTitle() == null) continue;
                    String cleanItemTitle = item.getTitle().replaceAll("\\s+", "").toLowerCase();

                    for (MetinItem dbItem : dbMetinler) {
                        if (dbItem.getTitle() != null) {
                            String cleanDbTitle = dbItem.getTitle().replaceAll("\\s+", "").toLowerCase();
                            if (cleanDbTitle.equals(cleanItemTitle)) {
                                item.setSaved(dbItem.isSaved());
                                break;
                            }
                        }
                    }
                }
            }

            List<kaydedilenler> finalRawList = rawList;
            runOnUiThread(() -> {
                masterList = finalRawList;
                filterCategory(currentSelectedCategory, getButtonForCategory(currentSelectedCategory));
            });
        }).start();
    }

    private void filterCategory(String categoryName, TextView selectedButton) {
        currentSelectedCategory = categoryName;

        for (TextView btn : categoryButtons) {
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.bg_search_bar); // Seçili olmayan buton arka planı
                btn.setTextColor(Color.parseColor("#CCCCCC")); // Aydınlık/Okunabilir Açık Gri
            }
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_black_icon_box); // Seçili olan buton
            selectedButton.setTextColor(Color.WHITE); // Tam Beyaz
        }


        if (masterList != null) {
            displayList.clear();
            java.util.Locale trLocale = new java.util.Locale("tr", "TR");
            String cleanQuery = currentSearchQuery.toLowerCase(trLocale).trim();

            for (kaydedilenler item : masterList) {
                if (item != null) {
                    // 1. Kategori Kontrolü
                    boolean matchesCategory = false;
                    if (categoryName.equalsIgnoreCase("TÜMÜ")) {
                        matchesCategory = true;
                    } else if (item.getCategory() != null) {
                        String itemCategory = item.getCategory().toLowerCase(trLocale).trim();
                        String targetCategory = categoryName.toLowerCase(trLocale).trim();
                        if (itemCategory.contains(targetCategory) || targetCategory.contains(itemCategory)) {
                            matchesCategory = true;
                        }
                    } else {
                        matchesCategory = true;
                    }

                    // 2. Arama Sorgusu Kontrolü (Başlık, İçerik ve Açıklamada Arama)
                    boolean matchesSearch = true;
                    if (!cleanQuery.isEmpty()) {
                        String title = item.getTitle() != null ? item.getTitle().toLowerCase(trLocale) : "";
                        String content = item.getContent() != null ? item.getContent().toLowerCase(trLocale) : "";
                        String description = item.getDescription() != null ? item.getDescription().toLowerCase(trLocale) : "";

                        matchesSearch = title.contains(cleanQuery) || content.contains(cleanQuery) || description.contains(cleanQuery);
                    }

                    if (matchesCategory && matchesSearch) {
                        displayList.add(item);
                    }
                }
            }

            adapter = new kaydedilenler_adapter(this, displayList);
            adapter.setOnItemClickListener(item -> {
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

    private TextView getButtonForCategory(String categoryName) {
        switch (categoryName) {
            case "Pratik Hayat İçin Fayda": return btnPratikHayat;
            case "Dış Şeylere Karşı Savunma": return btnDisSeyler;
            case "Zihinsel Mekanizma & Mimari": return btnZihinselMekanizma;
            case "TÜMÜ": default: return btnAll;
        }
    }

    private String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}