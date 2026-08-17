package com.example.hadi_bakalm;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.kaydedilenler;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class noroplastite extends AppCompatActivity {

    private static final String TAG = "NoroplastiteActivity";
    private static final String PREF_NAME = "ReaderSettings";
    private static final String KEY_FONT_SIZE = "CONCEPT_FONT_SIZE";
    private static final String KEY_THEME = "CONCEPT_READING_THEME";
    private static final String JSON_FILE = "kavramlar.json";

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Gson GSON = new Gson();

    private ImageView btnBack;
    private ImageView btnMoreMenu;
    private FrameLayout btnDialogues;
    private FrameLayout btnImportance;
    private View contentDialogues;
    private View contentImportance;

    private TextView txtKaydet;
    private AppDatabase db;
    private ConceptItem_kavram currentConcept;

    private TextView txtConceptTitle;
    private TextView txtConceptDescription;
    private TextView txtPersonalNote;
    private TextView txtDialoguesContent;
    private TextView txtImportanceContent;

    private TextView txtDisclaimer;
    private ImageView icDisclaimer;

    private SharedPreferences sharedPreferences;
    private int currentFontSize = 16;
    private String currentTheme = "AYDINLIK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroplastite);

        db = AppDatabase.getInstance(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentFontSize = sharedPreferences.getInt(KEY_FONT_SIZE, 16);
        currentTheme = sharedPreferences.getString(KEY_THEME, "AYDINLIK");

        initViews();
        applyFontSizeToViews(currentFontSize);
        applyReadingTheme(currentTheme);

        String gelenKavram = getIntent().getStringExtra("KAVRAM_ADI");
        loadConceptDataAsync(gelenKavram);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMoreMenu = findViewById(R.id.btnMenu);
        LinearLayout btnKaydet = findViewById(R.id.btnSave);
        txtKaydet = findViewById(R.id.kaydet_butonu);

        txtConceptTitle = findViewById(R.id.txtConceptTitle);
        txtConceptDescription = findViewById(R.id.txtConceptDescription);
        txtPersonalNote = findViewById(R.id.txtPersonalNote);
        txtDialoguesContent = findViewById(R.id.txtDialoguesContent);
        txtImportanceContent = findViewById(R.id.txtImportanceContent);

        TextView btnCopy1 = findViewById(R.id.btnCopy1);
        TextView btnShare1 = findViewById(R.id.btnShare1);
        TextView btnCopy2 = findViewById(R.id.btnCopy2);
        TextView btnShare2 = findViewById(R.id.btnShare2);

        TextView btnCopyDialogues = findViewById(R.id.btnCopyDialogues);
        TextView btnShareDialogues = findViewById(R.id.btnShareDialogues);
        TextView btnCopyImportance = findViewById(R.id.btnCopyImportance);
        TextView btnShareImportance = findViewById(R.id.btnShareImportance);

        btnDialogues = findViewById(R.id.btnDialogues);
        btnImportance = findViewById(R.id.btnImportance);
        contentDialogues = findViewById(R.id.contentDialogues);
        contentImportance = findViewById(R.id.contentImportance);

        txtDisclaimer = findViewById(R.id.txtDisclaimer);
        icDisclaimer = findViewById(R.id.icDisclaimer);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnKaydet != null) {
            btnKaydet.setOnClickListener(v -> toggleSaveState());
        }

        if (btnMoreMenu != null) {
            btnMoreMenu.setOnClickListener(this::showPopupMenu);
        }

        if (btnDialogues != null && contentDialogues != null) {
            btnDialogues.setOnClickListener(v -> {
                int visibility = contentDialogues.getVisibility();
                contentDialogues.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        if (btnImportance != null && contentImportance != null) {
            btnImportance.setOnClickListener(v -> {
                int visibility = contentImportance.getVisibility();
                contentImportance.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        setupActionButtons(btnCopy1, btnShare1, btnCopy2, btnShare2,
                btnCopyDialogues, btnShareDialogues, btnCopyImportance, btnShareImportance);
    }

    private void setupActionButtons(TextView c1, TextView s1, TextView c2, TextView s2,
                                    TextView cd, TextView sd, TextView ci, TextView si) {
        if (c1 != null && txtConceptDescription != null) {
            c1.setOnClickListener(v -> copyToClipboard("Kavram Açıklaması", txtConceptDescription.getText().toString()));
        }
        if (s1 != null && txtConceptDescription != null) {
            s1.setOnClickListener(v -> shareText("Kavram Açıklaması", txtConceptDescription.getText().toString()));
        }
        if (c2 != null && txtPersonalNote != null) {
            c2.setOnClickListener(v -> copyToClipboard("Kişisel Not", txtPersonalNote.getText().toString()));
        }
        if (s2 != null && txtPersonalNote != null) {
            s2.setOnClickListener(v -> shareText("Kişisel Not", txtPersonalNote.getText().toString()));
        }
        if (cd != null && txtDialoguesContent != null) {
            cd.setOnClickListener(v -> copyToClipboard("Örnek Diyaloglar", txtDialoguesContent.getText().toString()));
        }
        if (sd != null && txtDialoguesContent != null) {
            sd.setOnClickListener(v -> shareText("Örnek Diyaloglar", txtDialoguesContent.getText().toString()));
        }
        if (ci != null && txtImportanceContent != null) {
            ci.setOnClickListener(v -> copyToClipboard("Pratik Hayattaki Önemi", txtImportanceContent.getText().toString()));
        }
        if (si != null && txtImportanceContent != null) {
            si.setOnClickListener(v -> shareText("Pratik Hayattaki Önemi", txtImportanceContent.getText().toString()));
        }
    }

    private void toggleSaveState() {
        if (currentConcept == null) return;

        boolean newSavedState = !currentConcept.isSaved();
        currentConcept.setSaved(newSavedState);

        DB_EXECUTOR.execute(() -> {
            if (db != null) {
                db.conceptDao_kavram().update(currentConcept);
            }
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                updateSaveButtonUI();
                String msg = newSavedState ? "Kayıtlı kavramlara eklendi" : "Kayıtlı kavramlardan çıkarıldı";
                Toast.makeText(noroplastite.this, msg, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadConceptDataAsync(String rawKavramAdi) {
        String finalKavramAdi = (rawKavramAdi == null || rawKavramAdi.trim().isEmpty()) ? "Bilişsel Atrofi" : rawKavramAdi;

        DB_EXECUTOR.execute(() -> {
            kaydedilenler jsonConcept = getConceptFromJSON(finalKavramAdi);

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

            final String searchTitle = title;
            final String finalDesc = desc;
            final String finalNote = note;
            final String finalDialogues = dialogues;
            final String finalImportance = importance;

            ConceptItem_kavram foundConcept = null;
            if (db != null) {
                List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
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
                    currentConcept = new ConceptItem_kavram(searchTitle, finalDesc, finalNote, finalDialogues, finalImportance, false);
                    long newId = db.conceptDao_kavram().insert(currentConcept);
                    currentConcept.setId((int) newId);
                }

                if (currentConcept != null) {
                    currentConcept.setLastViewedTime(System.currentTimeMillis());
                    db.conceptDao_kavram().update(currentConcept);
                }
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (txtConceptTitle != null) txtConceptTitle.setText(searchTitle);
                if (txtConceptDescription != null) txtConceptDescription.setText(finalDesc);

                if (txtPersonalNote != null) {
                    if (!finalNote.trim().isEmpty()) {
                        txtPersonalNote.setText(finalNote);
                        if (txtPersonalNote.getParent() != null && txtPersonalNote.getParent().getParent() instanceof View) {
                            ((View) txtPersonalNote.getParent().getParent()).setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (txtPersonalNote.getParent() != null && txtPersonalNote.getParent().getParent() instanceof View) {
                            ((View) txtPersonalNote.getParent().getParent()).setVisibility(View.GONE);
                        }
                    }
                }

                if (txtDialoguesContent != null) txtDialoguesContent.setText(finalDialogues);
                if (txtImportanceContent != null) txtImportanceContent.setText(finalImportance);

                if (btnDialogues != null) {
                    btnDialogues.setVisibility(!finalDialogues.trim().isEmpty() ? View.VISIBLE : View.GONE);
                }

                if (btnImportance != null) {
                    btnImportance.setVisibility(!finalImportance.trim().isEmpty() ? View.VISIBLE : View.GONE);
                }

                updateSaveButtonUI();
            });
        });
    }

    private kaydedilenler getConceptFromJSON(String targetTitle) {
        String jsonString = loadJSONFromAsset();
        if (jsonString != null && targetTitle != null) {
            try {
                Type listType = new TypeToken<List<kaydedilenler>>() {}.getType();
                List<kaydedilenler> list = GSON.fromJson(jsonString, listType);

                if (list != null) {
                    for (kaydedilenler item : list) {
                        if (item.getTitle() != null && item.getTitle().replaceAll("\\s+", "").equalsIgnoreCase(targetTitle.replaceAll("\\s+", ""))) {
                            return item;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "JSON parse hatası", e);
            }
        }
        return null;
    }

    private String loadJSONFromAsset() {
        try (InputStream is = getAssets().open(JSON_FILE)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) return null;
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Log.e(TAG, "Asset okuma hatası", ex);
            return null;
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
            if (txtDiag != null) txtDiag.setTextColor(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
            if (btnDialogues.getChildCount() > 1 && btnDialogues.getChildAt(1) instanceof ImageView) {
                ((ImageView) btnDialogues.getChildAt(1)).setColorFilter(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
            }
        }

        if (btnImportance != null) {
            btnImportance.setBackgroundColor(theme.equals("KARANLIK") ? Color.parseColor("#FFFFFF") : Color.parseColor("#0F172A"));
            TextView txtImp = btnImportance.findViewById(R.id.bu_kavram_onem);
            if (txtImp != null) txtImp.setTextColor(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
            if (btnImportance.getChildCount() > 1 && btnImportance.getChildAt(1) instanceof ImageView) {
                ((ImageView) btnImportance.getChildAt(1)).setColorFilter(theme.equals("KARANLIK") ? Color.parseColor("#0F172A") : Color.parseColor("#FFFFFF"));
            }
        }

        if (txtConceptTitle != null) txtConceptTitle.setTextColor(textColor);
        if (txtConceptDescription != null) txtConceptDescription.setTextColor(textColor);
        if (txtPersonalNote != null) txtPersonalNote.setTextColor(textColor);
        if (txtDialoguesContent != null) txtDialoguesContent.setTextColor(textColor);
        if (txtImportanceContent != null) txtImportanceContent.setTextColor(textColor);

        updateTextButtonColors(subTextColor);

        if (btnBack != null) btnBack.setColorFilter(textColor);
        if (btnMoreMenu != null) btnMoreMenu.setColorFilter(textColor);

        if (txtDisclaimer != null) txtDisclaimer.setTextColor(subTextColor);
        if (icDisclaimer != null) icDisclaimer.setColorFilter(subTextColor);

        sharedPreferences.edit().putString(KEY_THEME, theme).apply();
    }

    private void updateTextButtonColors(int subTextColor) {
        int[] buttonIds = {
                R.id.btnCopy1, R.id.btnShare1, R.id.btnCopy2, R.id.btnShare2,
                R.id.btnCopyDialogues, R.id.btnShareDialogues, R.id.btnCopyImportance, R.id.btnShareImportance
        };
        for (int id : buttonIds) {
            TextView btn = findViewById(id);
            if (btn != null) {
                btn.setTextColor(subTextColor);
            }
        }
    }

    @SuppressLint("InflateParams")
    private void showReadingSettingsBottomSheet() {
        if (isFinishing() || isDestroyed()) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_okuma_ayarlari_paneli, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView btnThemeAydinlik = dialogView.findViewById(R.id.btnThemeAydinlik);
        TextView btnThemeSarimsi = dialogView.findViewById(R.id.btnThemeSarimsi);
        TextView btnThemeKaranlik = dialogView.findViewById(R.id.btnThemeKaranlik);

        TextView btnDecrease = dialogView.findViewById(R.id.btnTextDecrease);
        TextView btnIncrease = dialogView.findViewById(R.id.btnTextIncrease);
        TextView txtFontSizeIndicator = dialogView.findViewById(R.id.txtFontSizeIndicator);

        if (txtFontSizeIndicator != null) {
            txtFontSizeIndicator.setText(String.format(java.util.Locale.US, "%d sp", currentFontSize));
        }

        if (btnThemeAydinlik != null) btnThemeAydinlik.setOnClickListener(v -> applyReadingTheme("AYDINLIK"));
        if (btnThemeSarimsi != null) btnThemeSarimsi.setOnClickListener(v -> applyReadingTheme("SARIMSI"));
        if (btnThemeKaranlik != null) btnThemeKaranlik.setOnClickListener(v -> applyReadingTheme("KARANLIK"));

        if (btnDecrease != null) {
            btnDecrease.setOnClickListener(v -> {
                if (currentFontSize > 12) {
                    currentFontSize--;
                    applyFontSizeToViews(currentFontSize);
                    if (txtFontSizeIndicator != null) txtFontSizeIndicator.setText(String.format(java.util.Locale.US, "%d sp", currentFontSize));
                    saveFontSize(currentFontSize);
                }
            });
        }

        if (btnIncrease != null) {
            btnIncrease.setOnClickListener(v -> {
                if (currentFontSize < 24) {
                    currentFontSize++;
                    applyFontSizeToViews(currentFontSize);
                    if (txtFontSizeIndicator != null) txtFontSizeIndicator.setText(String.format(java.util.Locale.US, "%d sp", currentFontSize));
                    saveFontSize(currentFontSize);
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void saveFontSize(int sizeSp) {
        sharedPreferences.edit().putInt(KEY_FONT_SIZE, sizeSp).apply();
    }

    private void updateSaveButtonUI() {
        if (txtKaydet != null && currentConcept != null) {
            txtKaydet.setText(currentConcept.isSaved() ? "kaydedildi" : "kaydet");
        }
    }

    @SuppressLint("InflateParams")
    private void showFeedbackDialog() {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = getLayoutInflater().inflate(R.layout.hatali_bilgi_oneri_gonder, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        EditText etFeedbackText = dialogView.findViewById(R.id.etFeedbackText);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSendFeedback = dialogView.findViewById(R.id.btnSendFeedback);

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSendFeedback != null) {
            btnSendFeedback.setOnClickListener(v -> {
                String feedbackMessage = etFeedbackText != null ? etFeedbackText.getText().toString().trim() : "";
                if (feedbackMessage.isEmpty()) {
                    Toast.makeText(this, "Lütfen bir mesaj yazın", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"destek@emailadresin.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim");
                emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackMessage);

                try {
                    startActivity(Intent.createChooser(emailIntent, "E-posta uygulamasını seçin:"));
                    dialog.dismiss();
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, "Cihazda e-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }


    @SuppressLint("InflateParams")
    private void showPopupMenu(View anchorView) {
        if (isFinishing() || isDestroyed()) return;

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
                StringBuilder fullText = new StringBuilder();
                if (txtConceptTitle != null && txtConceptTitle.getText() != null) {
                    fullText.append("KAVRAM: ").append(txtConceptTitle.getText()).append("\n\n");
                }
                if (txtConceptDescription != null && txtConceptDescription.getText() != null) {
                    fullText.append("AÇIKLAMA:\n").append(txtConceptDescription.getText()).append("\n\n");
                }
                if (txtPersonalNote != null && txtPersonalNote.getText() != null) {
                    fullText.append("KİŞİSEL NOT / ANALİZ:\n").append(txtPersonalNote.getText()).append("\n\n");
                }
                if (txtDialoguesContent != null && txtDialoguesContent.getText() != null && !txtDialoguesContent.getText().toString().isEmpty()) {
                    fullText.append("ÖRNEK DİYALOGLAR:\n").append(txtDialoguesContent.getText()).append("\n\n");
                }
                if (txtImportanceContent != null && txtImportanceContent.getText() != null && !txtImportanceContent.getText().toString().isEmpty()) {
                    fullText.append("PRATİK HAYATTAKİ ÖNEMİ:\n").append(txtImportanceContent.getText());
                }

                copyToClipboard("Tüm Sayfa", fullText.toString().trim());
                popupWindow.dismiss();
            });
        }

        if (menuSharePage != null) {
            menuSharePage.setOnClickListener(v -> {
                String shareBody = "";
                if (txtConceptTitle != null && txtConceptDescription != null) {
                    shareBody = txtConceptTitle.getText().toString() + "\n\n" + txtConceptDescription.getText().toString();
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
}