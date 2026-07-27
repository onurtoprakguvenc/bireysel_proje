package com.example.hadi_bakalm.model;

public class concept_kavram_model {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_CONCEPT = 1;

    private int itemType;
    private String kategoriAdi;
    private String kavramAdi;
    private String aciklama;

    // Kategori Başlığı İçin Constructor
    public concept_kavram_model(String kategoriAdi) {
        this.kategoriAdi = kategoriAdi;
        this.itemType = TYPE_CATEGORY;
    }

    // Kavram Kartı İçin Constructor
    public concept_kavram_model(String kavramAdi, String aciklama) {
        this.kavramAdi = kavramAdi;
        this.aciklama = aciklama;
        this.itemType = TYPE_CONCEPT;
    }

    public int getItemType() { return itemType; }
    public String getKategoriAdi() { return kategoriAdi; }
    public String getKavramAdi() { return kavramAdi; }
    public String getAciklama() { return aciklama; }
}