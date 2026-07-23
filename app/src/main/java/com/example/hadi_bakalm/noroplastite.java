package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class noroplastite extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtConceptTitle;
    private TextView txtConceptDesc;
    private TextView txtDevNote;
    // Diğer expandable (genişletilebilir) alanlar ve butonlar da buraya eklenecek...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroplastite); // image_6.png'deki arayüz XML'iniz

        // 1. XML Bileşenlerini Bağlama
        btnBack = findViewById(R.id.btnBack);
        txtConceptTitle = findViewById(R.id.txtConceptTitle);
        txtConceptDesc = findViewById(R.id.txtConceptDescription);
        txtDevNote = findViewById(R.id.txtPersonalNote);
        // ... diğerlerini de bağlayacaksın ...

        // 2. Geri Butonu Mantığı
        btnBack.setOnClickListener(v -> finish());

        // 3. Veriyi Doldurma
        // Adapter'dan "Nöroplastisite" ismiyle geliyoruz.
        // Bu Activity kendi içinde veriyi şu şekilde set edebilir:

        txtConceptTitle.setText("Nöroplastisite");
        txtConceptDesc.setText("Beynin deneyimlerle yeniden yapılanma ve değişme yeteneği. Sinapsların güçlenmesi veya zayıflaması sürecidir.");
        txtDevNote.setText("Bu kavram, 'Öğrenmeyi Öğrenme' konusunun temel taşıdır. Değişimin mümkün olduğunu bilimsel olarak kanıtlar.");

        // Örnek Diyaloglar ve Pratik Hayat alanları için de Expandable (genişletilebilir) mantığı buraya kuracağız.
        // Paylaş/Kopyala/Kaydet buton mantıkları da burada olacak.

    }
}