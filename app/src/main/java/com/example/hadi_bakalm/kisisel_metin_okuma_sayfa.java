package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.MetinItem;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class kisisel_metin_okuma_sayfa extends AppCompatActivity {

    private static final String PREF_NAME = "ReaderSettings";
    private static final String KEY_FONT_SIZE = "READING_FONT_SIZE";
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    private AppDatabase db;
    private MetinItem currentMetin;

    private ImageView imgBookmarkIcon;
    private TextView txtBookmarkStatus;
    private TextView txtAciklama;
    private EditText etPersonalNote;
    private TextView txtFontSizeIndicator;

    private SharedPreferences sharedPreferences;
    private int currentFontSize = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        db = AppDatabase.getInstance(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentFontSize = sharedPreferences.getInt(KEY_FONT_SIZE, 16);

        initViews();
        applyFontSize(currentFontSize);
        handleIntentData();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSettings = findViewById(R.id.btnSettings);
        View cardSettingsPanel = findViewById(R.id.cardSettingsPanel);

        txtAciklama = findViewById(R.id.txtMainContent);
        etPersonalNote = findViewById(R.id.etPersonalNote);

        TextView btnCopyMainText = findViewById(R.id.btnCopyMainText);
        TextView btnCopyNote = findViewById(R.id.btnCopyNote);

        View btnBookmarkSave = findViewById(R.id.btnBookmarkSave);
        imgBookmarkIcon = findViewById(R.id.imgBookmarkIcon);
        txtBookmarkStatus = findViewById(R.id.txtBookmarkStatus);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (cardSettingsPanel != null) {
            TextView btnTextDecrease = cardSettingsPanel.findViewById(R.id.btnTextDecrease);
            TextView btnTextIncrease = cardSettingsPanel.findViewById(R.id.btnTextIncrease);
            txtFontSizeIndicator = cardSettingsPanel.findViewById(R.id.txtFontSizeIndicator);

            if (btnTextDecrease != null) {
                btnTextDecrease.setOnClickListener(v -> {
                    if (currentFontSize > 12) {
                        currentFontSize--;
                        applyFontSize(currentFontSize);
                        saveFontSize(currentFontSize);
                    }
                });
            }

            if (btnTextIncrease != null) {
                btnTextIncrease.setOnClickListener(v -> {
                    if (currentFontSize < 24) {
                        currentFontSize++;
                        applyFontSize(currentFontSize);
                        saveFontSize(currentFontSize);
                    }
                });
            }

            cardSettingsPanel.setOnClickListener(v -> {});
        }

        if (btnSettings != null && cardSettingsPanel != null) {
            btnSettings.setOnClickListener(v -> {
                int visibility = cardSettingsPanel.getVisibility();
                cardSettingsPanel.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        if (btnCopyMainText != null && txtAciklama != null) {
            btnCopyMainText.setOnClickListener(v -> copyToClipboard("Metin", txtAciklama.getText().toString()));
        }

        if (btnCopyNote != null && etPersonalNote != null) {
            btnCopyNote.setOnClickListener(v -> copyToClipboard("Kişisel Not", etPersonalNote.getText().toString()));
        }

        if (btnBookmarkSave != null) {
            btnBookmarkSave.setOnClickListener(v -> toggleSaveState());
        }

        if (etPersonalNote != null) {
            etPersonalNote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentMetin != null) {
                        currentMetin.setPersonalNote(s != null ? s.toString() : "");
                        DB_EXECUTOR.execute(() -> {
                            if (db != null) {
                                db.metinDao().update(currentMetin);
                            }
                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        String title = "Varsayılan Başlık";
        String content = "Varsayılan İçerik";
        String readTime = "1 dk okuma";
        int metinId = -1;

        if (intent != null) {
            if (intent.hasExtra("METIN_ID")) {
                metinId = intent.getIntExtra("METIN_ID", -1);
            } else if (intent.hasExtra("ID")) {
                metinId = intent.getIntExtra("ID", -1);
            }

            String extraTitle = intent.getStringExtra("TITLE");
            if (extraTitle != null && !extraTitle.trim().isEmpty()) {
                title = extraTitle;
            }

            String extraContent = intent.getStringExtra("CONTENT");
            String extraDesc = intent.getStringExtra("DESCRIPTION");

            if (!TextUtils.isEmpty(extraContent)) {
                content = extraContent;
            } else if (!TextUtils.isEmpty(extraDesc)) {
                content = extraDesc;
            }

            String extraReadTime = intent.getStringExtra("READ_TIME");
            if (extraReadTime != null && !extraReadTime.trim().isEmpty()) {
                readTime = extraReadTime;
            }
        }

        TextView txtBaslik = findViewById(R.id.baslik);
        TextView txtReadTime = findViewById(R.id.txtReadTime);
        TextView btnShareMainText = findViewById(R.id.btnShareMainText);

        if (txtBaslik != null) txtBaslik.setText(title);
        if (txtAciklama != null) txtAciklama.setText(content);
        if (txtReadTime != null) txtReadTime.setText(readTime);

        if (btnShareMainText != null && txtAciklama != null) {
            String finalTitle = title;
            btnShareMainText.setOnClickListener(v -> shareText(finalTitle, txtAciklama.getText().toString()));
        }

        checkAndLoadDatabase(metinId, title, content);
    }

    private void copyToClipboard(String label, String text) {
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(this, "Kopyalanacak metin bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " panoya kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareText(String title, String text) {
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(this, "Paylaşılacak metin bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Şununla paylaş:"));
    }

    private void applyFontSize(int sizeSp) {
        if (txtAciklama != null) txtAciklama.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (etPersonalNote != null) etPersonalNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (txtFontSizeIndicator != null) txtFontSizeIndicator.setText(String.format(Locale.US, "%dsp", sizeSp));
    }

    private void saveFontSize(int sizeSp) {
        sharedPreferences.edit().putInt(KEY_FONT_SIZE, sizeSp).apply();
    }

    private void checkAndLoadDatabase(int targetId, String title, String content) {
        DB_EXECUTOR.execute(() -> {
            if (db == null) return;
            MetinItem matchedItem = null;

            if (targetId != -1) {
                matchedItem = db.metinDao().getMetinById(targetId);
            }

            if (matchedItem == null) {
                List<MetinItem> list = db.metinDao().getAllMetinler();
                if (list != null) {
                    for (MetinItem item : list) {
                        if (item.getTitle() != null && item.getTitle().equalsIgnoreCase(title)) {
                            matchedItem = item;
                            break;
                        }
                    }
                }
            }

            if (matchedItem == null) {
                matchedItem = new MetinItem(title, content, "", false);
                db.metinDao().insert(matchedItem);

                List<MetinItem> updatedList = db.metinDao().getAllMetinler();
                if (updatedList != null) {
                    for (MetinItem item : updatedList) {
                        if (item.getTitle() != null && item.getTitle().equalsIgnoreCase(title)) {
                            matchedItem = item;
                            break;
                        }
                    }
                }
            }

            matchedItem.setLastViewedTime(System.currentTimeMillis());
            db.metinDao().update(matchedItem);

            currentMetin = matchedItem;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (etPersonalNote != null && currentMetin.getPersonalNote() != null) {
                    etPersonalNote.setText(currentMetin.getPersonalNote());
                }
                updateBookmarkUI(currentMetin.isSaved());
            });
        });
    }

    private void toggleSaveState() {
        if (currentMetin == null) return;

        boolean newSaveState = !currentMetin.isSaved();
        currentMetin.setSaved(newSaveState);

        DB_EXECUTOR.execute(() -> {
            if (db != null) {
                db.metinDao().update(currentMetin);
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                updateBookmarkUI(newSaveState);
                String msg = newSaveState ? "Metin kaydedilenlere eklendi" : "Metin kaydedilenlerden çıkarıldı";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateBookmarkUI(boolean isSaved) {
        if (txtBookmarkStatus != null) {
            txtBookmarkStatus.setText(isSaved ? "Kaydedildi" : "Kaydet");
        }
        if (imgBookmarkIcon != null) {
            imgBookmarkIcon.setImageResource(isSaved ? R.drawable.ic_bookmark_save : R.drawable.ic_bookmark);
        }
    }
}