package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import java.util.Objects;

@SuppressWarnings("unused")
public class KisiselMetin {

    private final String baslik;
    private final String aciklama;

    public KisiselMetin(String baslik, String aciklama) {
        this.baslik = (baslik != null) ? baslik : "";
        this.aciklama = (aciklama != null) ? aciklama : "";
    }

    public String getBaslik() {
        return baslik;
    }

    public String getAciklama() {
        return aciklama;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KisiselMetin that = (KisiselMetin) o;
        return Objects.equals(baslik, that.baslik) &&
                Objects.equals(aciklama, that.aciklama);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baslik, aciklama);
    }

    @NonNull
    @Override
    public String toString() {
        return "KisiselMetin{" +
                "baslik='" + baslik + '\'' +
                ", aciklama='" + aciklama + '\'' +
                '}';
    }
}