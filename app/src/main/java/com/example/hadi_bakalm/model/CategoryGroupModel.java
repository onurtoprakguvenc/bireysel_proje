package com.example.hadi_bakalm.model;

import java.util.List;

public class CategoryGroupModel {
    private String kategoriBasligi;
    private List<concept_kavram_model> kavramlar;

    public CategoryGroupModel(String kategoriBasligi, List<concept_kavram_model> kavramlar) {
        this.kategoriBasligi = kategoriBasligi;
        this.kavramlar = kavramlar;
    }

    public String getKategoriBasligi() {
        return kategoriBasligi;
    }

    public void setKategoriBasligi(String kategoriBasligi) {
        this.kategoriBasligi = kategoriBasligi;
    }

    public List<concept_kavram_model> getKavramlar() {
        return kavramlar;
    }

    public void setKavramlar(List<concept_kavram_model> kavramlar) {
        this.kavramlar = kavramlar;
    }
}