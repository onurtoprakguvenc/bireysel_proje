package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class noroplastite extends AppCompatActivity {

    private ImageView btnBack, btnMoreMenu;
    private FrameLayout btnDialogues, btnImportance;
    private View contentDialogues, contentImportance;

    // Kaydet Butonu ve Veri Tabanı Bileşenleri
    private LinearLayout btnKaydet;
    private TextView txtKaydet;
    private AppDatabase db;
    private ConceptItem_kavram currentConcept;

    // Metin Boyutlandırılacak & Renklendirilecek TextView Listesi
    private TextView txtConceptTitle, txtConceptDescription, txtPersonalNote, txtDialoguesContent, txtImportanceContent;
    private TextView btnCopy1, btnShare1, btnCopy2, btnShare2;
    private TextView btnCopyDialogues, btnShareDialogues, btnCopyImportance, btnShareImportance;

    // SharedPreferences & Okuma Ayarları Değişkenleri
    private SharedPreferences sharedPreferences;
    private int currentFontSize = 16;
    private String currentTheme = "AYDINLIK";

    private void copyToClipboard(String label, String text) {
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(this, "Kopyalanacak metin bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroplastite);

        // --- 1. Veri Tabanı ve SharedPreferences ---
        db = AppDatabase.getInstance(this);
        sharedPreferences = getSharedPreferences("ReaderSettings", Context.MODE_PRIVATE);
        currentFontSize = sharedPreferences.getInt("CONCEPT_FONT_SIZE", 16);
        currentTheme = sharedPreferences.getString("CONCEPT_READING_THEME", "AYDINLIK");

        // --- 2. Görünüm Bağlantıları ---
        btnBack = findViewById(R.id.btnBack);
        btnMoreMenu = findViewById(R.id.btnMenu);
        btnKaydet = findViewById(R.id.btnSave);
        txtKaydet = findViewById(R.id.kaydet_butonu);

        txtConceptTitle = findViewById(R.id.txtConceptTitle);
        txtConceptDescription = findViewById(R.id.txtConceptDescription);
        txtPersonalNote = findViewById(R.id.txtPersonalNote);
        txtDialoguesContent = findViewById(R.id.txtDialoguesContent);
        txtImportanceContent = findViewById(R.id.txtImportanceContent);

        btnCopy1 = findViewById(R.id.btnCopy1);
        btnShare1 = findViewById(R.id.btnShare1);
        btnCopy2 = findViewById(R.id.btnCopy2);
        btnShare2 = findViewById(R.id.btnShare2);

        btnCopyDialogues = findViewById(R.id.btnCopyDialogues);
        btnShareDialogues = findViewById(R.id.btnShareDialogues);
        btnCopyImportance = findViewById(R.id.btnCopyImportance);
        btnShareImportance = findViewById(R.id.btnShareImportance);

        btnDialogues = findViewById(R.id.btnDialogues);
        btnImportance = findViewById(R.id.btnImportance);
        contentDialogues = findViewById(R.id.contentDialogues);
        contentImportance = findViewById(R.id.contentImportance);

        // Kaydedilmiş olan metin boyutunu ve temasını uygula
        applyFontSizeToViews(currentFontSize);
        applyReadingTheme(currentTheme);

        // --- 3. Tıklanan Kavrama Göre Verileri Dinamik Doldur ---
        String gelenKavram = getIntent().getStringExtra("KAVRAM_ADI");
        setupConceptData(gelenKavram, txtConceptTitle, txtConceptDescription, txtPersonalNote, txtDialoguesContent, txtImportanceContent);

        // --- 4. Kaydet Butonu Tıklama Olayı (GÜNCELLEME) ---
        if (btnKaydet != null) {
            btnKaydet.setOnClickListener(v -> {
                if (currentConcept != null) {
                    boolean newSavedState = !currentConcept.isSaved();
                    currentConcept.setSaved(newSavedState);

                    new Thread(() -> {
                        db.conceptDao_kavram().update(currentConcept);
                        runOnUiThread(() -> {
                            updateSaveButtonUI();
                            String msg = newSavedState ? "Kayıtlı kavramlara eklendi" : "Kayıtlı kavramlardan çıkarıldı";
                            Toast.makeText(noroplastite.this, msg, Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                }
            });
        }

        // --- 5. 3 Nokta Menüsü Tıklama Olayı ---
        if (btnMoreMenu != null) {
            btnMoreMenu.setOnClickListener(this::showPopupMenu);
        }

        // --- 6. Kopyala / Paylaş Tıklama Olayları ---
        if (btnCopy1 != null && txtConceptDescription != null) {
            btnCopy1.setOnClickListener(v -> copyToClipboard("Kavram Açıklaması", txtConceptDescription.getText().toString()));
        }

        if (btnShare1 != null && txtConceptDescription != null) {
            btnShare1.setOnClickListener(v -> shareText("Kavram Açıklaması", txtConceptDescription.getText().toString()));
        }

        if (btnCopy2 != null && txtPersonalNote != null) {
            btnCopy2.setOnClickListener(v -> copyToClipboard("Kişisel Not", txtPersonalNote.getText().toString()));
        }

        if (btnShare2 != null && txtPersonalNote != null) {
            btnShare2.setOnClickListener(v -> shareText("Kişisel Not", txtPersonalNote.getText().toString()));
        }

        if (btnCopyDialogues != null && txtDialoguesContent != null) {
            btnCopyDialogues.setOnClickListener(v -> copyToClipboard("Örnek Diyaloglar", txtDialoguesContent.getText().toString()));
        }

        if (btnShareDialogues != null && txtDialoguesContent != null) {
            btnShareDialogues.setOnClickListener(v -> shareText("Örnek Diyaloglar", txtDialoguesContent.getText().toString()));
        }

        if (btnCopyImportance != null && txtImportanceContent != null) {
            btnCopyImportance.setOnClickListener(v -> copyToClipboard("Pratik Hayattaki Önemi", txtImportanceContent.getText().toString()));
        }

        if (btnShareImportance != null && txtImportanceContent != null) {
            btnShareImportance.setOnClickListener(v -> shareText("Pratik Hayattaki Önemi", txtImportanceContent.getText().toString()));
        }

        // --- 7. Geri Butonu ve Açılır/Kapanır Panel Mantıkları ---
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnDialogues != null && contentDialogues != null) {
            btnDialogues.setOnClickListener(v -> {
                contentDialogues.setVisibility(contentDialogues.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        if (btnImportance != null && contentImportance != null) {
            btnImportance.setOnClickListener(v -> {
                contentImportance.setVisibility(contentImportance.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }
    }

    private void applyFontSizeToViews(int sizeSp) {
        if (txtConceptDescription != null) txtConceptDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (txtPersonalNote != null) txtPersonalNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (txtDialoguesContent != null) txtDialoguesContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (txtImportanceContent != null) txtImportanceContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
    }

    private void applyReadingTheme(String theme) {
        currentTheme = theme;

        int rootBgColor;
        int cardBgColor;
        int textColor;
        int subTextColor;

        switch (theme) {
            case "SARIMSI":
                rootBgColor = Color.parseColor("#FDF6E3");
                cardBgColor = Color.parseColor("#F5E6C8");
                textColor = Color.parseColor("#433422");
                subTextColor = Color.parseColor("#6B5B45");
                break;

            case "KARANLIK":
                rootBgColor = Color.parseColor("#0F172A");
                cardBgColor = Color.parseColor("#1E293B");
                textColor = Color.parseColor("#FFFFFF");
                subTextColor = Color.parseColor("#94A3B8");
                break;

            case "AYDINLIK":
            default:
                rootBgColor = Color.parseColor("#FFFFFF");
                cardBgColor = Color.parseColor("#F8FAFC");
                textColor = Color.parseColor("#0F172A");
                subTextColor = Color.parseColor("#475569");
                break;
        }

        View root = findViewById(R.id.topBar) != null ? (View) findViewById(R.id.topBar).getParent() : null;
        if (root != null) {
            root.setBackgroundColor(rootBgColor);
        }

        if (txtConceptDescription != null && txtConceptDescription.getParent() != null && txtConceptDescription.getParent().getParent() instanceof MaterialCardView) {
            ((MaterialCardView) txtConceptDescription.getParent().getParent()).setCardBackgroundColor(cardBgColor);
        }
        if (txtPersonalNote != null && txtPersonalNote.getParent() != null && txtPersonalNote.getParent().getParent() instanceof MaterialCardView) {
            ((MaterialCardView) txtPersonalNote.getParent().getParent()).setCardBackgroundColor(cardBgColor);
        }
        if (contentDialogues instanceof LinearLayout) {
            contentDialogues.setBackgroundColor(cardBgColor);
        }
        if (contentImportance instanceof LinearLayout) {
            contentImportance.setBackgroundColor(cardBgColor);
        }

        if (btnDialogues != null) {
            btnDialogues.setBackgroundColor(theme.equals("KARANLIK") ? Color.parseColor("#FFFFFF") : Color.parseColor("#0F172A"));

            TextView txtDiag = btnDialogues.findViewById(R.id.ornek_diyalog);
            ImageView imgDiag = (ImageView) btnDialogues.getChildAt(1);
            if (txtDiag != null) txtDiag.setTextColor(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
            if (imgDiag != null) imgDiag.setColorFilter(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
        }

        if (btnImportance != null) {
            btnImportance.setBackgroundColor(theme.equals("KARANLIK") ? Color.parseColor("#FFFFFF") : Color.parseColor("#0F172A"));

            TextView txtImp = btnImportance.findViewById(R.id.bu_kavram_onem);
            ImageView imgImp = (ImageView) btnImportance.getChildAt(1);
            if (txtImp != null) txtImp.setTextColor(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
            if (imgImp != null) imgImp.setColorFilter(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
        }

        if (txtConceptTitle != null) txtConceptTitle.setTextColor(textColor);
        if (txtConceptDescription != null) txtConceptDescription.setTextColor(textColor);
        if (txtPersonalNote != null) txtPersonalNote.setTextColor(textColor);
        if (txtDialoguesContent != null) txtDialoguesContent.setTextColor(textColor);
        if (txtImportanceContent != null) txtImportanceContent.setTextColor(textColor);

        if (btnCopy1 != null) btnCopy1.setTextColor(subTextColor);
        if (btnShare1 != null) btnShare1.setTextColor(subTextColor);
        if (btnCopy2 != null) btnCopy2.setTextColor(subTextColor);
        if (btnShare2 != null) btnShare2.setTextColor(subTextColor);
        if (btnCopyDialogues != null) btnCopyDialogues.setTextColor(subTextColor);
        if (btnShareDialogues != null) btnShareDialogues.setTextColor(subTextColor);
        if (btnCopyImportance != null) btnCopyImportance.setTextColor(subTextColor);
        if (btnShareImportance != null) btnShareImportance.setTextColor(subTextColor);

        if (btnBack != null) btnBack.setColorFilter(textColor);
        if (btnMoreMenu != null) btnMoreMenu.setColorFilter(textColor);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CONCEPT_READING_THEME", theme);
        editor.apply();
    }

    private void showReadingSettingsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_okuma_ayarlari_paneli, null);

        dialogView.setVisibility(View.VISIBLE);
        bottomSheetDialog.setContentView(dialogView);

        TextView btnThemeAydinlik = dialogView.findViewById(R.id.btnThemeAydinlik);
        TextView btnThemeSarimsi = dialogView.findViewById(R.id.btnThemeSarimsi);
        TextView btnThemeKaranlik = dialogView.findViewById(R.id.btnThemeKaranlik);

        TextView btnDecrease = dialogView.findViewById(R.id.btnTextDecrease);
        TextView btnIncrease = dialogView.findViewById(R.id.btnTextIncrease);
        TextView txtFontSizeIndicator = dialogView.findViewById(R.id.txtFontSizeIndicator);

        if (txtFontSizeIndicator != null) {
            txtFontSizeIndicator.setText(currentFontSize + " sp");
        }

        if (btnThemeAydinlik != null) {
            btnThemeAydinlik.setOnClickListener(v -> applyReadingTheme("AYDINLIK"));
        }
        if (btnThemeSarimsi != null) {
            btnThemeSarimsi.setOnClickListener(v -> applyReadingTheme("SARIMSI"));
        }
        if (btnThemeKaranlik != null) {
            btnThemeKaranlik.setOnClickListener(v -> applyReadingTheme("KARANLIK"));
        }

        if (btnDecrease != null) {
            btnDecrease.setOnClickListener(v -> {
                if (currentFontSize > 12) {
                    currentFontSize--;
                    applyFontSizeToViews(currentFontSize);
                    if (txtFontSizeIndicator != null) txtFontSizeIndicator.setText(currentFontSize + " sp");
                    saveFontSize(currentFontSize);
                }
            });
        }

        if (btnIncrease != null) {
            btnIncrease.setOnClickListener(v -> {
                if (currentFontSize < 24) {
                    currentFontSize++;
                    applyFontSizeToViews(currentFontSize);
                    if (txtFontSizeIndicator != null) txtFontSizeIndicator.setText(currentFontSize + " sp");
                    saveFontSize(currentFontSize);
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void saveFontSize(int sizeSp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("CONCEPT_FONT_SIZE", sizeSp);
        editor.apply();
    }

    // --- ROOM DB İŞLEMLERİ ARKA PLANA ALINDI VE EŞLEŞTİRME SAĞLANDI ---
    // --- ROOM DB İŞLEMLERİ ARKA PLANA ALINDI VE EŞLEŞTİRME SAĞLANDI ---
    private void setupConceptData(String kavramAdi, TextView txtTitle, TextView txtDesc, TextView txtNote, TextView txtDialogues, TextView txtImportance) {
        String finalKavramAdi = (kavramAdi == null || kavramAdi.trim().isEmpty()) ? "Bilişsel Atrofi" : kavramAdi;

        com.example.hadi_bakalm.model.kaydedilenler jsonConcept = getConceptFromJSON(finalKavramAdi);

        String title = finalKavramAdi;
        String desc = "";
        String note = "";
        String dialogues = "";
        String importance = "";

        if (jsonConcept != null) {
            if (jsonConcept.getTitle() != null) title = jsonConcept.getTitle();
            if (jsonConcept.getDescription() != null) desc = jsonConcept.getDescription();
            if (jsonConcept.getPersonalNote() != null) note = jsonConcept.getPersonalNote();
            if (jsonConcept.getDialogues() != null) dialogues = jsonConcept.getDialogues();
            if (jsonConcept.getImportance() != null) importance = jsonConcept.getImportance();
        }

        if (txtTitle != null) txtTitle.setText(title);
        if (txtDesc != null) txtDesc.setText(desc);

        // Kişisel not yoksa ikinci kartı ekranda tamamen gizle (View.GONE)
        if (txtNote != null) {
            if (note != null && !note.trim().isEmpty()) {
                txtNote.setText(note);
                if (txtNote.getParent() != null && txtNote.getParent().getParent() instanceof View) {
                    ((View) txtNote.getParent().getParent()).setVisibility(View.VISIBLE);
                }
            } else {
                if (txtNote.getParent() != null && txtNote.getParent().getParent() instanceof View) {
                    ((View) txtNote.getParent().getParent()).setVisibility(View.GONE);
                }
            }
        }

        if (txtDialogues != null) txtDialogues.setText(dialogues);
        if (txtImportance != null) txtImportance.setText(importance);

        // Açılır butonların içeriğe göre görünürlüğü
        if (btnDialogues != null) {
            btnDialogues.setVisibility((dialogues != null && !dialogues.trim().isEmpty()) ? View.VISIBLE : View.GONE);
        }

        if (btnImportance != null) {
            btnImportance.setVisibility((importance != null && !importance.trim().isEmpty()) ? View.VISIBLE : View.GONE);
        }

        // --- VERİTABANI ARAMA VE EŞLEŞTİRME DÜZELTMESİ ---
        final String searchTitle = title;
        final String finalDesc = desc;
        final String finalNote = note;
        final String finalDialogues = dialogues;
        final String finalImportance = importance;

        new Thread(() -> {
            List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
            ConceptItem_kavram foundConcept = null;

            if (allConcepts != null) {
                for (ConceptItem_kavram item : allConcepts) {
                    if (item.getTitle() != null && item.getTitle().replaceAll("\\s+", "").equalsIgnoreCase(searchTitle.replaceAll("\\s+", ""))) {
                        foundConcept = item;
                        break;
                    }
                }
            }

            if (foundConcept != null) {
                currentConcept = foundConcept;
            } else {
                // Veritabanında yoksa sıfırdan ekle
                currentConcept = new ConceptItem_kavram(searchTitle, finalDesc, finalNote, finalDialogues, finalImportance, false);
                long newId = db.conceptDao_kavram().insert(currentConcept);
                currentConcept.setId((int) newId);
            }

            if (currentConcept != null) {
                currentConcept.setLastViewedTime(System.currentTimeMillis());
                db.conceptDao_kavram().update(currentConcept);
            }

            // Arayüzdeki kaydet butonunu veritabanı durumuna göre güncelle
            runOnUiThread(this::updateSaveButtonUI);
        }).start();
    }

    private com.example.hadi_bakalm.model.kaydedilenler getConceptFromJSON(String targetTitle) {
        String jsonString = loadJSONFromAssetForNoroplastite("kavramlar.json");
        if (jsonString != null && targetTitle != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<com.example.hadi_bakalm.model.kaydedilenler>>() {}.getType();
                List<com.example.hadi_bakalm.model.kaydedilenler> list = gson.fromJson(jsonString, listType);

                if (list != null) {
                    for (com.example.hadi_bakalm.model.kaydedilenler item : list) {
                        if (item.getTitle() != null && item.getTitle().replaceAll("\\s+", "").equalsIgnoreCase(targetTitle.replaceAll("\\s+", ""))) {
                            return item;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void updateSaveButtonUI() {
        if (txtKaydet != null && currentConcept != null) {
            txtKaydet.setText(currentConcept.isSaved() ? "kaydedildi" : "kaydet");
        }
    }

    private void showFeedbackDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.hatali_bilgi_oneri_gonder, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        EditText etFeedbackText = dialogView.findViewById(R.id.etFeedbackText);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSendFeedback = dialogView.findViewById(R.id.btnSendFeedback);

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSendFeedback != null) {
            btnSendFeedback.setOnClickListener(v -> {
                String feedbackMessage = etFeedbackText.getText().toString().trim();

                if (feedbackMessage.isEmpty()) {
                    Toast.makeText(this, "Lütfen bir mesaj yazın", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"destek@emailadresin.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim");
                emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackMessage);

                try {
                    startActivity(Intent.createChooser(emailIntent, "E-posta uygulamasını seçin:"));
                    dialog.dismiss();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "Cihazda e-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    private void showPopupMenu(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.kavram_sayfa_uc_nokta_menu, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(16f);

        LinearLayout menuCopyAll = popupView.findViewById(R.id.menuCopyAll);
        LinearLayout menuSharePage = popupView.findViewById(R.id.menuSharePage);
        LinearLayout menuReportError = popupView.findViewById(R.id.menuReportError);
        LinearLayout menuFontSize = popupView.findViewById(R.id.menuFontSize);

        if (menuCopyAll != null) {
            menuCopyAll.setOnClickListener(v -> {
                TextView txtTitle = findViewById(R.id.txtConceptTitle);
                TextView txtDesc = findViewById(R.id.txtConceptDescription);
                TextView txtNote = findViewById(R.id.txtPersonalNote);
                TextView txtDialogues = findViewById(R.id.txtDialoguesContent);
                TextView txtImportance = findViewById(R.id.txtImportanceContent);

                StringBuilder fullText = new StringBuilder();

                if (txtTitle != null && txtTitle.getText() != null) {
                    fullText.append("KAVRAM: ").append(txtTitle.getText().toString()).append("\n\n");
                }
                if (txtDesc != null && txtDesc.getText() != null) {
                    fullText.append("AÇIKLAMA:\n").append(txtDesc.getText().toString()).append("\n\n");
                }
                if (txtNote != null && txtNote.getText() != null) {
                    fullText.append("KİŞİSEL NOT / ANALİZ:\n").append(txtNote.getText().toString()).append("\n\n");
                }
                if (txtDialogues != null && txtDialogues.getText() != null && !txtDialogues.getText().toString().isEmpty()) {
                    fullText.append("ÖRNEK DİYALOGLAR:\n").append(txtDialogues.getText().toString()).append("\n\n");
                }
                if (txtImportance != null && txtImportance.getText() != null && !txtImportance.getText().toString().isEmpty()) {
                    fullText.append("PRATİK HAYATTAKİ ÖNEMİ:\n").append(txtImportance.getText().toString());
                }

                copyToClipboard("Tüm Sayfa", fullText.toString().trim());
                popupWindow.dismiss();
            });
        }

        if (menuSharePage != null) {
            menuSharePage.setOnClickListener(v -> {
                TextView txtTitle = findViewById(R.id.txtConceptTitle);
                TextView txtDesc = findViewById(R.id.txtConceptDescription);

                String shareBody = "";
                if (txtTitle != null && txtDesc != null) {
                    shareBody = txtTitle.getText().toString() + "\n\n" + txtDesc.getText().toString();
                }

                shareText("Kavram Detayı", shareBody);
                popupWindow.dismiss();
            });
        }

        if (menuReportError != null) {
            menuReportError.setOnClickListener(v -> {
                popupWindow.dismiss();
                showFeedbackDialog();
            });
        }

        if (menuFontSize != null) {
            menuFontSize.setOnClickListener(v -> {
                popupWindow.dismiss();
                showReadingSettingsBottomSheet();
            });
        }

        popupWindow.showAsDropDown(anchorView, -100, 10);
    }

    private String loadJSONFromAssetForNoroplastite(String fileName) {
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