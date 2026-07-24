package com.example.hadi_bakalm;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.concept_kavram_adapter;
import com.example.hadi_bakalm.model.concept_kavram_model;

import java.util.ArrayList;
import java.util.List;

public class kavramlar_sayfa extends AppCompatActivity {

    private RecyclerView recyclerViewConcepts;
    private concept_kavram_adapter adapter;
    private List<concept_kavram_model> liste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kavramlar_sayfa);

        recyclerViewConcepts = findViewById(R.id.recyclerViewMainCategories);
        recyclerViewConcepts.setLayoutManager(new LinearLayoutManager(this));

        liste = new ArrayList<>();

        // 1. Kategori Başlığı ve Altındaki Kavramlar
        liste.add(new concept_kavram_model("Nörokimya / Fizyoloji"));
        liste.add(new concept_kavram_model("Nöroplastisite", "Beynin deneyimlerle yeniden yapılanma yeteneği.", "Nörokimya / Fizyoloji"));
        liste.add(new concept_kavram_model("Synaptic Pruning", "Kullanılmayan sinaptik bağlantıların budanması.", "Nörokimya / Fizyoloji"));

        // 2. Kategori Başlığı ve Altındaki Kavramlar
        liste.add(new concept_kavram_model("Bilişsel Psikoloji"));
        liste.add(new concept_kavram_model("Bilişsel Yük", "Zihnin aynı anda işleyebileceği bilgi miktarı.", "Bilişsel Psikoloji"));

        // Adapter'ı bağla
        adapter = new concept_kavram_adapter(liste);
        recyclerViewConcepts.setAdapter(adapter);
    }
}