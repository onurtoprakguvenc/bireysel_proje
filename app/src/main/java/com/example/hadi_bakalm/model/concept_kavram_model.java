package com.example.hadi_bakalm.model;

public class concept_kavram_model {

    // Eleman türlerini ayırmak için sabitler
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_CONCEPT = 1;

    private int itemType;
    private String kategoriAdi;
    private String kavramAdi;
    private String aciklama;

    // 1. Kurucu Metot: Kategori Başlığı Oluşturmak İçin
    public concept_kavram_model(String kategoriAdi) {
        this.itemType = TYPE_CATEGORY;
        this.kategoriAdi = kategoriAdi;
    }

    // 2. Kurucu Metot: Kavram Kartı Oluşturmak İçin
    public concept_kavram_model(String kavramAdi, String aciklama, String kategoriAdi) {
        this.itemType = TYPE_CONCEPT;
        this.kavramAdi = kavramAdi;
        this.aciklama = aciklama;
        this.kategoriAdi = kategoriAdi;
    }

    // Getter Metotları
    public int getItemType() {
        return itemType;
    }

    public String getKategoriAdi() {
        return kategoriAdi;
    }

    public String getKavramAdi() {
        return kavramAdi;
    }

    public String getAciklama() {
        return aciklama;
    }
}