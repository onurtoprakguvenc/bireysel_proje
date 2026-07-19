package com.example.hadi_bakalm.model;

public class Concept {

    private String name;
    private String aciklama;          // kavram açıklaması normal
    private String kisiselNot;        // benim kişisel geliştirici notum
    private String ornekDiyaloglar;   // örnek diyaloglar
    private String pratikOnemi;       // pratik hayattaki önemi
    private String vucutEtkisi;       // vücut içinde yarattığı etki

    // Sadece isimle hızlı oluşturmak için (mevcut kullanım, geriye dönük uyumlu)
    public Concept(String name) {
        this.name = name;
    }

    // Detay ekranı için tüm alanlarla oluşturmak istersen
    public Concept(String name, String aciklama, String kisiselNot,
                   String ornekDiyaloglar, String pratikOnemi, String vucutEtkisi) {
        this.name = name;
        this.aciklama = aciklama;
        this.kisiselNot = kisiselNot;
        this.ornekDiyaloglar = ornekDiyaloglar;
        this.pratikOnemi = pratikOnemi;
        this.vucutEtkisi = vucutEtkisi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getKisiselNot() {
        return kisiselNot;
    }

    public void setKisiselNot(String kisiselNot) {
        this.kisiselNot = kisiselNot;
    }

    public String getOrnekDiyaloglar() {
        return ornekDiyaloglar;
    }

    public void setOrnekDiyaloglar(String ornekDiyaloglar) {
        this.ornekDiyaloglar = ornekDiyaloglar;
    }

    public String getPratikOnemi() {
        return pratikOnemi;
    }

    public void setPratikOnemi(String pratikOnemi) {
        this.pratikOnemi = pratikOnemi;
    }

    public String getVucutEtkisi() {
        return vucutEtkisi;
    }

    public void setVucutEtkisi(String vucutEtkisi) {
        this.vucutEtkisi = vucutEtkisi;
    }
}
