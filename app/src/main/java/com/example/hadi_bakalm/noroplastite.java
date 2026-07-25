package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class noroplastite extends AppCompatActivity {

    private ImageView btnBack, btnMoreMenu;
    private FrameLayout btnDialogues, btnImportance;
    private View contentDialogues, contentImportance;

    // Kaydet Butonu Bileşenleri
    private LinearLayout btnKaydet;
    private TextView txtKaydet;
    private SharedPreferences sharedPreferences;
    private boolean isSaved = false;
    private final String kavramId = "kavram_noroplastite"; // Bu sayfaya özel benzersiz anahtar

    // Panoya Kopyalama Yardımcı Metodu
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " panoya kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    // Metin Paylaşma Yardımcı Metodu
    private void shareText(String title, String text) {
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

        // --- 1. Görünüm Bağlantıları (XML ID'leri) ---
        btnBack = findViewById(R.id.btnBack);
        btnMoreMenu = findViewById(R.id.btnMenu);      // XML'de 3 nokta ikonu ID'si
        btnKaydet = findViewById(R.id.btnSave);          // Kaydet Butonu (LinearLayout)
        txtKaydet = findViewById(R.id.kaydet_butonu);    // Kaydet Butonu Yazısı (TextView)

        // 1. Kart (Kavram Açıklaması)
        TextView btnCopy1 = findViewById(R.id.btnCopy1);
        TextView btnShare1 = findViewById(R.id.btnShare1);
        TextView txtConceptDescription = findViewById(R.id.txtConceptDescription);

        // 2. Kart (Kişisel Not)
        TextView btnCopy2 = findViewById(R.id.btnCopy2);
        TextView btnShare2 = findViewById(R.id.btnShare2);
        TextView txtPersonalNote = findViewById(R.id.txtPersonalNote);

        // Paneller
        btnDialogues = findViewById(R.id.btnDialogues);
        btnImportance = findViewById(R.id.btnImportance);
        contentDialogues = findViewById(R.id.contentDialogues);
        contentImportance = findViewById(R.id.contentImportance);

        // --- 2. Kaydet Durumunu Hafızadan Okuma (SharedPreferences) ---
        sharedPreferences = getSharedPreferences("SavedKavramlar", MODE_PRIVATE);
        isSaved = sharedPreferences.getBoolean(kavramId, false);
        updateSaveButtonUI();

        // Kaydet Butonu Tıklama Olayı
        if (btnKaydet != null) {
            btnKaydet.setOnClickListener(v -> {
                isSaved = !isSaved; // Durumu değiştir

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(kavramId, isSaved);
                editor.apply();

                updateSaveButtonUI();

                if (isSaved) {
                    Toast.makeText(this, "Kayıtlı kavramlara eklendi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Kayıtlı kavramlardan çıkarıldı", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- 3. 3 Nokta Menüsü Tıklama Olayı ---
        if (btnMoreMenu != null) {
            btnMoreMenu.setOnClickListener(this::showPopupMenu);
        }

        // --- 4. Kopyala / Paylaş Tıklama Olayları ---
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

        // --- 5. Geri Butonu ve Panel Açılır/Kapanır Mantıkları ---
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

    // Kaydet Butonu Yazısını Güncelleyen Yordam
    private void updateSaveButtonUI() {
        if (txtKaydet != null) {
            if (isSaved) {
                txtKaydet.setText("kaydedildi");
            } else {
                txtKaydet.setText("kaydet");
            }
        }
    }

    // Geri Bildirim Pop-up Diyalogu
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

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

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
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim - Nöroplastisite");
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

    // 3 Nokta Pop-up Menüsü
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
                copyToClipboard("Tüm Sayfa", "Nöroplastisite kavram metni ve notları...");
                popupWindow.dismiss();
            });
        }

        if (menuSharePage != null) {
            menuSharePage.setOnClickListener(v -> {
                shareText("Nöroplastisite", "Nöroplastisite kavram detayları...");
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
                Toast.makeText(this, "Yazı boyutu ayarı açılacak", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
        }

        popupWindow.showAsDropDown(anchorView, -100, 10);
    }
}