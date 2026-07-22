package com.example.hadi_bakalm.model;

public class KisiselMetin {
    private String baslik;
    private String aciklama;

    public KisiselMetin(String baslik, String aciklama) {
        this.baslik = baslik;
        this.aciklama = aciklama;
    }

    public String getBaslik() {
        return baslik;
    }

    public String getAciklama() {
        return aciklama;
    }
}