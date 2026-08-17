package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import java.util.Objects;

@SuppressWarnings("unused")
public class SonIncelemeModel {

    private final long id;
    private final String baslik;
    private final String aciklama;
    private final String zaman;
    private final String tur;

    public SonIncelemeModel(long id, String baslik, String aciklama, String zaman, String tur) {
        this.id = id;
        this.baslik = (baslik != null) ? baslik : "";
        this.aciklama = (aciklama != null) ? aciklama : "";
        this.zaman = (zaman != null) ? zaman : "";
        this.tur = (tur != null) ? tur : "";
    }

    public long getId() {
        return id;
    }

    public String getBaslik() {
        return baslik;
    }

    public String getAciklama() {
        return aciklama;
    }

    public String getZaman() {
        return zaman;
    }

    public String getTur() {
        return tur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SonIncelemeModel that = (SonIncelemeModel) o;
        return id == that.id &&
                Objects.equals(baslik, that.baslik) &&
                Objects.equals(aciklama, that.aciklama) &&
                Objects.equals(zaman, that.zaman) &&
                Objects.equals(tur, that.tur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, baslik, aciklama, zaman, tur);
    }

    @NonNull
    @Override
    public String toString() {
        return "SonIncelemeModel{" +
                "id=" + id +
                ", baslik='" + baslik + '\'' +
                ", tur='" + tur + '\'' +
                ", zaman='" + zaman + '\'' +
                '}';
    }
}