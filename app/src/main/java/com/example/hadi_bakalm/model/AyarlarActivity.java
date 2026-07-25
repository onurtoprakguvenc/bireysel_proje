package com.example.hadi_bakalm.model; // Kendi paket adınızı girin

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.R;

public class AyarlarActivity extends AppCompatActivity {

    private Spinner spinnerTheme;
    private Spinner spinnerAutoClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ayarlar_sayfa);

        // Arayüz Elemanlarını Bağlama
        initViews();

        // Açılır Menüleri (Spinner) Doldurma
        setupSpinners();

        // Tıklama Olaylarını Tanımlama
        setupClickListeners();
    }

    private void initViews() {
        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerAutoClear = findViewById(R.id.spinnerAutoClear);
    }

    private void setupSpinners() {
        // 1. Tema Seçenekleri
        String[] themeOptions = {"Aydınlık", "Karanlık", "Sistem Varsayılanı"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                themeOptions
        );
        spinnerTheme.setAdapter(themeAdapter);

        // 2. Geçmişi Otomatik Temizle Seçenekleri
        String[] clearOptions = {"Asla", "Haftalık", "Aylık"};
        ArrayAdapter<String> clearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                clearOptions
        );
        spinnerAutoClear.setAdapter(clearAdapter);
    }

    private void setupClickListeners() {
        // Verileri Dışa Aktar Butonu
        findViewById(R.id.btnExportData).setOnClickListener(v -> {
            Toast.makeText(this, "Veriler JSON formatında dışa aktarılıyor...", Toast.LENGTH_SHORT).show();
        });

        // Tüm Verileri Sıfırla Butonu
        findViewById(R.id.btnResetAll).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tüm Verileri Sıfırla")
                    .setMessage("Kaydedilen tüm içerikler ve inceleme geçmişi kalıcı olarak silinecektir. Onaylıyor musunuz?")
                    .setPositiveButton("Sıfırla", (dialog, which) -> {
                        // Veri sıfırlama işlemi burada yürütülecek
                        Toast.makeText(this, "Tüm veriler başarıyla sıfırlandı.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Vazgeç", null)
                    .show();
        });
    }
}