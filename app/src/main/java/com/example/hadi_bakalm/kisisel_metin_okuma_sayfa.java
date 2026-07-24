package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class kisisel_metin_okuma_sayfa extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kisisel_metin_okuma_sayfa);

        // XML elemanlarını bağlama
        TextView txtBaslik = findViewById(R.id.baslık);
        TextView txtAciklama = findViewById(R.id.txtMainContent);

        // Intent ile gelen verileri alma ("TITLE" ve "DESCRIPTION" anahtarlarıyla)
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("TITLE");
            String description = intent.getStringExtra("DESCRIPTION");

            if (txtBaslik != null && title != null) {
                txtBaslik.setText(title);
            }
            if (txtAciklama != null && description != null) {
                txtAciklama.setText(description);
            }
        }
    }
}