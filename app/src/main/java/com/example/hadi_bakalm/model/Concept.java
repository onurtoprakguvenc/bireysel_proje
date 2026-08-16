package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import java.util.Objects;

@SuppressWarnings("unused")
public class Concept {

    private String name;
    private String aciklama;          // kavram açıklaması normal
    private String kisiselNot;        // kişisel geliştirici notu
    private String ornekDiyaloglar;   // örnek diyaloglar
    private String pratikOnemi;       // pratik hayattaki önemi

    // Sadece isimle hızlı oluşturmak için
    public Concept(String name) {
        this.name = (name != null) ? name : "";
        this.aciklama = "";
        this.kisiselNot = "";
        this.ornekDiyaloglar = "";
        this.pratikOnemi = "";
    }

    // Detay ekranı için tüm alanlarla oluşturmak için
    public Concept(String name, String aciklama, String kisiselNot,
                   String ornekDiyaloglar, String pratikOnemi, String s) {
        this.name = (name != null) ? name : "";
        this.aciklama = (aciklama != null) ? aciklama : "";
        this.kisiselNot = (kisiselNot != null) ? kisiselNot : "";
        this.ornekDiyaloglar = (ornekDiyaloglar != null) ? ornekDiyaloglar : "";
        this.pratikOnemi = (pratikOnemi != null) ? pratikOnemi : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null) ? name : "";
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = (aciklama != null) ? aciklama : "";
    }

    public String getKisiselNot() {
        return kisiselNot;
    }

    public void setKisiselNot(String kisiselNot) {
        this.kisiselNot = (kisiselNot != null) ? kisiselNot : "";
    }

    public String getOrnekDiyaloglar() {
        return ornekDiyaloglar;
    }

    public void setOrnekDiyaloglar(String ornekDiyaloglar) {
        this.ornekDiyaloglar = (ornekDiyaloglar != null) ? ornekDiyaloglar : "";
    }

    public String getPratikOnemi() {
        return pratikOnemi;
    }

    public void setPratikOnemi(String pratikOnemi) {
        this.pratikOnemi = (pratikOnemi != null) ? pratikOnemi : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return Objects.equals(name, concept.name) &&
                Objects.equals(aciklama, concept.aciklama) &&
                Objects.equals(kisiselNot, concept.kisiselNot) &&
                Objects.equals(ornekDiyaloglar, concept.ornekDiyaloglar) &&
                Objects.equals(pratikOnemi, concept.pratikOnemi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, aciklama, kisiselNot, ornekDiyaloglar, pratikOnemi);
    }

    @NonNull
    @Override
    public String toString() {
        return "Concept{" +
                "name='" + name + '\'' +
                ", aciklama='" + aciklama + '\'' +
                ", kisiselNot='" + kisiselNot + '\'' +
                ", ornekDiyaloglar='" + ornekDiyaloglar + '\'' +
                ", pratikOnemi='" + pratikOnemi + '\'' +
                '}';
    }
}