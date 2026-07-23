package com.example.hadi_bakalm.model;

public class concept_kavram_model {
    private String kavramAdi;
    private String aciklama;

    // Constructor (Kurucu Metot)
    public concept_kavram_model(String kavramAdi, String aciklama) {
        this.kavramAdi = kavramAdi;
        this.aciklama = aciklama;
    }

    // Getter ve Setter Metotları
    public String getKavramAdi() {
        return kavramAdi;
    }

    public void setKavramAdi(String kavramAdi) {
        this.kavramAdi = kavramAdi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }
}