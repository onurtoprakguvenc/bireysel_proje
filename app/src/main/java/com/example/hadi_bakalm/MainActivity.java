package com.example.hadi_bakalm;

import android.os.Bundle;
import com.example.hadi_bakalm.data.ConceptRepository;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.adapter.ConceptAdapter;
import com.example.hadi_bakalm.model.Concept;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvYeniGelenKavramlar;
    private ConceptAdapter conceptAdapter;
    private List<Concept> conceptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvYeniGelenKavramlar = findViewById(R.id.yeni_gelen_kavramlar_btn);

        // Veri artık burada tutulmuyor, merkezi depodan (repository) okunuyor
        conceptList = ConceptRepository.getAllConcepts();

        conceptAdapter = new ConceptAdapter(conceptList);
        rvYeniGelenKavramlar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvYeniGelenKavramlar.setAdapter(conceptAdapter);
    }
}


//planlar değişti.arayüz değişti