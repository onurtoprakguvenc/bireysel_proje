package com.example.hadi_bakalm;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    private RecyclerView recyclerView;
    private concept_kavram_adapter adapter;
    private final List<CategoryGroupModel> anaListe = new ArrayList<>();
    private final List<CategoryGroupModel> goruntulenenListe = new ArrayList<>();

    private LinearLayout chipContainer;
    private TextView txtConceptCountBadge;
    private ImageView btnSearchConcept;

    // Arayüz İçi Arama Bileşenleri
    private RelativeLayout searchBarContainer;
    private EditText etSearchInput;
    private ImageView btnClearSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kavramlar_sayfa);

        initViews();
        setupListeners();

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            adapter = new concept_kavram_adapter(goruntulenenListe);
            recyclerView.setAdapter(adapter);
        }

        loadConceptDataAsync();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        chipContainer = findViewById(R.id.conceptChipContainer);
        recyclerView = findViewById(R.id.recyclerViewMainCategories);
        btnSearchConcept = findViewById(R.id.btnSearchConcept);

        searchBarContainer = findViewById(R.id.searchBarContainer);
        etSearchInput = findViewById(R.id.etSearchInput);
        btnClearSearchText = findViewById(R.id.btnClearSearchText);
    }

    private void setupListeners() {
        // Büyütece tıklandığında popup yerine arayüzdeki arama çubuğunu aç/kapat
        if (btnSearchConcept != null) {
            btnSearchConcept.setOnClickListener(v -> {
                if (searchBarContainer == null) return;

                if (searchBarContainer.getVisibility() == View.VISIBLE) {
                    searchBarContainer.setVisibility(View.GONE);
                    if (etSearchInput != null) {
                        etSearchInput.setText("");
                        hideKeyboard(etSearchInput);
                    }
                    filterConcepts("");
                } else {
                    searchBarContainer.setVisibility(View.VISIBLE);
                    if (etSearchInput != null) {
                        etSearchInput.requestFocus();
                        showKeyboard(etSearchInput);
                    }
                }
            });
        }

        // Arama çubuğundaki temizle / kapat (X) butonu
        if (btnClearSearchText != null) {
            btnClearSearchText.setOnClickListener(v -> {
                if (etSearchInput != null) {
                    etSearchInput.setText("");
                }
                filterConcepts("");
            });
        }

        // Arama kutusuna yazı yazıldıkça anlık filtreleme
        if (etSearchInput != null) {
            etSearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterConcepts(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterConcepts(String query) {
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase();
        goruntulenenListe.clear();

        if (cleanQuery.isEmpty()) {
            goruntulenenListe.addAll(anaListe);
        } else {
            for (CategoryGroupModel group : anaListe) {
                List<concept_kavram_model> eslesenler = new ArrayList<>();
                if (group.getKavramlar() != null) {
                    for (concept_kavram_model item : group.getKavramlar()) {
                        boolean adEslesiyor = item.getKavramAdi() != null && item.getKavramAdi().toLowerCase().contains(cleanQuery);
                        boolean aciklamaEslesiyor = item.getAciklama() != null && item.getAciklama().toLowerCase().contains(cleanQuery);
                        boolean kategoriEslesiyor = group.getKategoriBasligi() != null && group.getKategoriBasligi().toLowerCase().contains(cleanQuery);

                        if (adEslesiyor || aciklamaEslesiyor || kategoriEslesiyor) {
                            eslesenler.add(item);
                        }
                    }
                }
                if (!eslesenler.isEmpty()) {
                    goruntulenenListe.add(new CategoryGroupModel(group.getKategoriBasligi(), eslesenler));
                }
            }
        }

        if (adapter != null) {
            adapter.updateData(goruntulenenListe);
        }
        updateBadgeCount(goruntulenenListe);
    }

    private void showKeyboard(View view) {
        view.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

                goruntulenenListe.clear();
                goruntulenenListe.addAll(anaListe);

                if (adapter != null) {
                    adapter.updateData(goruntulenenListe);
                }

                updateBadgeCount(goruntulenenListe);
                setupCategoryChips();
            });
        }).start();
    }

    private void updateBadgeCount(List<CategoryGroupModel> liste) {
        if (txtConceptCountBadge == null) return;
        int total = 0;
        for (CategoryGroupModel g : liste) {
            if (g.getKavramlar() != null) {
                total += g.getKavramlar().size();
            }
        }
        txtConceptCountBadge.setText(total + " kavram");
    }

    private void setupCategoryChips() {
        if (chipContainer == null) return;
        chipContainer.removeAllViews();

        int toplamKavramSayisi = 0;
        for (CategoryGroupModel group : anaListe) {
            if (group.getKavramlar() != null) {
                toplamKavramSayisi += group.getKavramlar().size();
            }
        }

        addChipView("Tümü (" + toplamKavramSayisi + ")", -1, true);

        for (int i = 0; i < anaListe.size(); i++) {
            CategoryGroupModel group = anaListe.get(i);
            String title = group.getKategoriBasligi();
            String displayTitle = title.contains("/") ? title.split("/")[0].trim() : title;
            addChipView(displayTitle, i, false);
        }
    }

    private void addChipView(String text, int targetIndex, boolean isSelectedDefault) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) (32 * getResources().getDisplayMetrics().density)
        );
        params.setMarginEnd((int) (6 * getResources().getDisplayMetrics().density));
        chip.setLayoutParams(params);

        chip.setPadding(
                (int) (14 * getResources().getDisplayMetrics().density),
                0,
                (int) (14 * getResources().getDisplayMetrics().density),
                0
        );
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setText(text);
        chip.setTextSize(12);

        if (isSelectedDefault) {
            chip.setBackgroundResource(R.drawable.bg_black_pill);
            chip.setTextColor(0xFFFFFFFF);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(0xFF475569);
        }

        chip.setOnClickListener(v -> {
            updateChipSelectionUI(chip);

            goruntulenenListe.clear();
            if (targetIndex == -1) {
                goruntulenenListe.addAll(anaListe);
            } else if (targetIndex < anaListe.size()) {
                goruntulenenListe.add(anaListe.get(targetIndex));
            }

            if (adapter != null) {
                adapter.updateData(goruntulenenListe);
            }
            updateBadgeCount(goruntulenenListe);

            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
        });

        chipContainer.addView(chip);
    }

    private void updateChipSelectionUI(TextView selectedChip) {
        if (chipContainer == null) return;

        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            View child = chipContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (tv == selectedChip) {
                    tv.setBackgroundResource(R.drawable.bg_black_pill);
                    tv.setTextColor(0xFFFFFFFF);
                    tv.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    tv.setBackgroundResource(R.drawable.bg_chip_inactive);
                    tv.setTextColor(0xFF475569);
                    tv.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }
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