package com.example.hadi_bakalm.model;

@SuppressWarnings("unused")
public class KisiselMetin {
    private final String baslik;
    private final String aciklama;

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