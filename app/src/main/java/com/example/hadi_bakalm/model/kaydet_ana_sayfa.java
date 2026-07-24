package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.kaydedilenler_adapter;
import com.example.hadi_bakalm.model.kaydedilenler;

import java.util.ArrayList;
import java.util.List;

public class kaydet_ana_sayfa extends AppCompatActivity {

    private RecyclerView recyclerViewSaved;
    private kaydedilenler_adapter adapter;
    private List<kaydedilenler> savedList;
    private TextView txtItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kaydet_sayfa_gorme);

        recyclerViewSaved = findViewById(R.id.recyclerViewSaved);
        txtItemCount = findViewById(R.id.txtItemCount);

        recyclerViewSaved.setLayoutManager(new LinearLayoutManager(this));

        // Test Verileri
        savedList = new ArrayList<>();
        savedList.add(new kaydedilenler("1", "başlık 1 – düşünce mimarisi", "Zihinsel modeller, dünyayı anlama ve karmaşık durumları basitleştirme şeklimizi belirler.", "METİN", "Kişisel Not", "Dün eklendi"));
        savedList.add(new kaydedilenler("2", "batık maliyet yanılsaması", "Geçmişte harcanan zaman veya para yüzünden zararlı bir karara devam etme eğilimi.", "KAVRAM", "Karar Teorisi", "3 gün önce eklendi"));
        savedList.add(new kaydedilenler("3", "başlık 3 – dijital sadeleşme", "Gürültüden uzaklaşmak ve zihinsel sakinlik sağlamak için sadeleşme rehberi.", "METİN", "Minimalizm", "1 ay önce eklendi"));

        // Sayaç Güncelleme
        if (txtItemCount != null) {
            txtItemCount.setText(savedList.size() + " İçerik");
        }

        // Adapter Bağlama
        adapter = new kaydedilenler_adapter(this, savedList);
        recyclerViewSaved.setAdapter(adapter);
    }
}