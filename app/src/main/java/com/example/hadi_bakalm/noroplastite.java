package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
        btnMoreMenu = findViewById(R.id.btnMenu); // XML'de 3 nokta ikonu ID'si
        btnKaydet = findViewById(R.id.btnSave);     // Kaydet Butonu (LinearLayout veya FrameLayout)

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

    // 3 Nokta Pop-up Menüsü
    private void showPopupMenu(View anchorView) {
        // 1. Özel layout'u inflate et
        View popupView = getLayoutInflater().inflate(R.layout.kavram_sayfa_uc_nokta_menu, null);

        // 2. PopupWindow oluştur
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true // Dışarıya tıklanınca kapanması için
        );

        // Gölge görünürlüğü ve arka plan ayarı
        popupWindow.setElevation(16f);

        // 3. Menü içi tıklamaları bağla
        LinearLayout menuCopyAll = popupView.findViewById(R.id.menuCopyAll);
        LinearLayout menuSharePage = popupView.findViewById(R.id.menuSharePage);
        LinearLayout menuReportError = popupView.findViewById(R.id.menuReportError);
        LinearLayout menuFontSize = popupView.findViewById(R.id.menuFontSize);

        // Tümünü Kopyala
        if (menuCopyAll != null) {
            menuCopyAll.setOnClickListener(v -> {
                copyToClipboard("Tüm Sayfa", "Nöroplastisite kavram metni ve notları...");
                popupWindow.dismiss();
            });
        }

        // Sayfayı Paylaş
        if (menuSharePage != null) {
            menuSharePage.setOnClickListener(v -> {
                shareText("Nöroplastisite", "Nöroplastisite kavram detayları...");
                popupWindow.dismiss();
            });
        }

        // Hatalı Bilgi / Öneri
        if (menuReportError != null) {
            menuReportError.setOnClickListener(v -> {
                Toast.makeText(this, "Geri bildirim ekranı açılacak", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
        }

        // Yazı Boyutu Ayarlama
        if (menuFontSize != null) {
            menuFontSize.setOnClickListener(v -> {
                Toast.makeText(this, "Yazı boyutu ayarı açılacak", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
        }

        // 4. Pop-up'ı 3 noktanın hemen altında göster
        popupWindow.showAsDropDown(anchorView, -100, 10);
    }
}