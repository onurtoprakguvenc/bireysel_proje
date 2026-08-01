package com.example.hadi_bakalm.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;
import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.kisisel_metin_okuma_sayfa;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
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

        // Tıklama Olayları
        if (btnAll != null) btnAll.setOnClickListener(v -> filterCategory("TÜMÜ", btnAll));
        if (btnPratikHayat != null) btnPratikHayat.setOnClickListener(v -> filterCategory("Pratik Hayat İçin Fayda", btnPratikHayat));
        if (btnDisSeyler != null) btnDisSeyler.setOnClickListener(v -> filterCategory("Dış Şeylere Karşı Savunma", btnDisSeyler));
        if (btnZihinselMekanizma != null) btnZihinselMekanizma.setOnClickListener(v -> filterCategory("Zihinsel Mekanizma & Mimari", btnZihinselMekanizma));
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
                Type listType = new TypeToken<List<kaydedilenler>>() {}.getType();
                rawList = gson.fromJson(jsonString, listType);
            }

            AppDatabase db = AppDatabase.getInstance(KisiselMetinlerimActivity.this);
            List<ConceptItem_kavram> dbConcepts = db.conceptDao_kavram().getAllConceptler();

            if (dbConcepts != null && !dbConcepts.isEmpty() && rawList != null) {
                for (kaydedilenler item : rawList) {
                    for (ConceptItem_kavram concept : dbConcepts) {
                        if (concept.getTitle() != null && concept.getTitle().equalsIgnoreCase(item.getTitle())) {
                            item.setSaved(concept.isSaved());
                            break;
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
                btn.setBackgroundResource(R.drawable.bg_black_icon_box);
                btn.setTextColor(Color.parseColor("#888888"));
            }
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_black_icon_box);
            selectedButton.setTextColor(Color.WHITE);
        }

        if (masterList != null) {
            displayList.clear();
            if (categoryName.equalsIgnoreCase("TÜMÜ")) {
                displayList.addAll(masterList);
            } else {
                for (kaydedilenler item : masterList) {
                    if (item.getCategory() != null && item.getCategory().equalsIgnoreCase(categoryName)) {
                        displayList.add(item);
                    }
                }
            }

            // Kart Tıklama Dinleyicisinin (OnItemClickListener) Bağlanması
            if (adapter == null) {
                adapter = new kaydedilenler_adapter(this, displayList);

                // Kart Tıklama İşlemi
                adapter.setOnItemClickListener(item -> {
                    Intent intent = new Intent(KisiselMetinlerimActivity.this, kisisel_metin_okuma_sayfa.class); // Kendi detay Activity isminizi yazın
                    intent.putExtra("title", item.getTitle());
                    intent.putExtra("content", item.getContent());
                    intent.putExtra("category", item.get());
                    startActivity(intent);
                });

                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
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