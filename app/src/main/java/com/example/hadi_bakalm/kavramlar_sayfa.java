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
    private List<concept_kavram_model> kavramListesi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kavramlar_sayfa); // XML dosya adın

        // 1. XML üzerindeki RecyclerView'ı bağla
        recyclerViewConcepts = findViewById(R.id.recyclerViewMainCategories); // XML'deki RecyclerView ID'niz
        recyclerViewConcepts.setLayoutManager(new LinearLayoutManager(this));

        // 2. Örnek test verilerini doldur
        kavramListesi = new ArrayList<>();
        kavramListesi.add(new concept_kavram_model("Nöroplastisite", "Beynin deneyimlerle yeniden yapılanma yeteneği."));
        kavramListesi.add(new concept_kavram_model("Bilişsel Yük", "Zihnin aynı anda işleyebileceği bilgi miktarı."));

        // 3. Adapter'ı oluştur ve RecyclerView'a bağla
        adapter = new concept_kavram_adapter(kavramListesi);
        recyclerViewConcepts.setAdapter(adapter);
    }
}