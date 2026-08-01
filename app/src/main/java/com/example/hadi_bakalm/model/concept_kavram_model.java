package com.example.hadi_bakalm.model;

public class concept_kavram_model {
    private String id;
    private String kavramAdi;
    private String aciklama;
    private String icerik; // JSON'daki "content" alanı için

    // Standart Kullanım Constructor
    public concept_kavram_model(String kavramAdi, String aciklama) {
        this.kavramAdi = kavramAdi;
        this.aciklama = aciklama;
    }

    // JSON'dan Tam Veri Çekerken Kullanılacak Constructor
    public concept_kavram_model(String id, String kavramAdi, String aciklama, String icerik) {
        this.id = id;
        this.kavramAdi = kavramAdi;
        this.aciklama = aciklama;
        this.icerik = icerik;
    }

    // Getter Metotları
    public String getId() { return id; }
    public String getKavramAdi() { return kavramAdi; }
    public String getAciklama() { return aciklama; }
    public String getIcerik() { return icerik; }
}