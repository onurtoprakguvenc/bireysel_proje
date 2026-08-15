package com.example.hadi_bakalm.model;

public class SonIncelemeModel {
    private final long id; // int yerine long yapıldı (Room Entity uyumu için)
    private final String baslik;
    private final String aciklama;
    private final String zaman;
    private final String tur;

    public SonIncelemeModel(long id, String baslik, String aciklama, String zaman, String tur) {
        this.id = id;
        this.baslik = baslik;
        this.aciklama = aciklama;
        this.zaman = zaman;
        this.tur = tur;
    }

    public long getId() { return id; }
    public String getBaslik() { return baslik; }
    public String getAciklama() { return aciklama; }
    public String getZaman() { return zaman; }
    public String getTur() { return tur; }
}