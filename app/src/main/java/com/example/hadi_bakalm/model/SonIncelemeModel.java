package com.example.hadi_bakalm.model;

public class SonIncelemeModel {
    private String baslik;
    private String aciklama;
    private String zaman;
    private String tur; // "Kavram" veya "Metin"

    public SonIncelemeModel(String baslik, String aciklama, String zaman, String tur) {
        this.baslik = baslik;
        this.aciklama = aciklama;
        this.zaman = zaman;
        this.tur = tur;
    }

    public String getBaslik() { return baslik; }
    public String getAciklama() { return aciklama; }
    public String getZaman() { return zaman; }
    public String getTur() { return tur; }
}