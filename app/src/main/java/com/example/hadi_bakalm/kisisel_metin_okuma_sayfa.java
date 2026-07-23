package com.example.hadi_bakalm;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.R;

public class kisisel_metin_okuma_sayfa extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        // Intent ile gelen verileri alma
        String baslik = getIntent().getStringExtra("baslik");
        String aciklama = getIntent().getStringExtra("aciklama");

        // XML elemanlarını bağlama (XML id'lerine göre güncelleyebilirsin)
        TextView txtBaslik = findViewById(R.id.baslık);
        TextView txtAciklama = findViewById(R.id.txtMainContent);

        if (baslik != null) txtBaslik.setText(baslik);
        if (aciklama != null) txtAciklama.setText(aciklama);
    }
}