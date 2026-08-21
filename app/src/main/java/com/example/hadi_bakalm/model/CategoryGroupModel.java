package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;


import java.util.List;
import java.util.Objects;

public class CategoryGroupModel {

    private final String kategoriBasligi;
    private final List<concept_kavram_model> kavramlar;

    public CategoryGroupModel(String kategoriBasligi, List<concept_kavram_model> kavramlar) {
        this.kategoriBasligi = (kategoriBasligi != null) ? kategoriBasligi : "";
        this.kavramlar = (kavramlar != null) ? List.copyOf(kavramlar) : List.of();
    }



    public String getKategoriBasligi() {
        return kategoriBasligi;
    }

    public List<concept_kavram_model> getKavramlar() {
        return kavramlar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryGroupModel that = (CategoryGroupModel) o;
        return Objects.equals(kategoriBasligi, that.kategoriBasligi) &&
                Objects.equals(kavramlar, that.kavramlar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kategoriBasligi, kavramlar);
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryGroupModel{" +
                "kategoriBasligi='" + kategoriBasligi + '\'' +
                ", kavramlarCount=" + kavramlar.size() +
                '}';
    }
}