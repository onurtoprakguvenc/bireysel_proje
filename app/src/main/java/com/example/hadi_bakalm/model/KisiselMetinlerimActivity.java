package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.KisiselMetinAdapter;
import com.example.hadi_bakalm.model.KisiselMetin;

import java.util.ArrayList;
import java.util.List;

public class KisiselMetinlerimActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ana_sayfa_kisisel_metinlerim);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPersonalTexts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<KisiselMetin> liste = new ArrayList<>();

        ImageView btnBack = findViewById(R.id.btnBack); // XML'deki id neyse o (örn: btnBack)

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish(); // Ekranı kapatır ve önceki ekrana döner
            });
        }

        liste.add(new KisiselMetin("Nöroplastisite Notları", "Beynin yapısının deneyimlerle değişimi."));
        liste.add(new KisiselMetin("Bilişsel Haritalar", "Mekansal hafıza ve öğrenme süreçleri."));
        liste.add(new KisiselMetin("Odaklanma Protokolü", "Günlük çalışma ve dikkat süreleri."));

        KisiselMetinAdapter adapter = new KisiselMetinAdapter(liste);
        recyclerView.setAdapter(adapter);
    }
}