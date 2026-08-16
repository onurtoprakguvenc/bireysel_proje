package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import java.util.Objects;

@SuppressWarnings("unused")
public class concept_kavram_model {

    private final String id;
    private final String kavramAdi;
    private final String aciklama;
    private final String icerik;

    // Standart Kullanım Constructor
    public concept_kavram_model(String kavramAdi, String aciklama) {
        this("", kavramAdi, aciklama, "");
    }

    // JSON'dan Tam Veri Çekerken Kullanılacak Constructor
    public concept_kavram_model(String id, String kavramAdi, String aciklama, String icerik) {
        this.id = (id != null) ? id : "";
        this.kavramAdi = (kavramAdi != null) ? kavramAdi : "";
        this.aciklama = (aciklama != null) ? aciklama : "";
        this.icerik = (icerik != null) ? icerik : "";
    }

    public String getId() {
        return id;
    }

    public String getKavramAdi() {
        return kavramAdi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public String getIcerik() {
        return icerik;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        concept_kavram_model that = (concept_kavram_model) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(kavramAdi, that.kavramAdi) &&
                Objects.equals(aciklama, that.aciklama) &&
                Objects.equals(icerik, that.icerik);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kavramAdi, aciklama, icerik);
    }

    @NonNull
    @Override
    public String toString() {
        return "concept_kavram_model{" +
                "id='" + id + '\'' +
                ", kavramAdi='" + kavramAdi + '\'' +
                ", aciklama='" + aciklama + '\'' +
                ", icerikLength=" + icerik.length() +
                '}';
    }
}