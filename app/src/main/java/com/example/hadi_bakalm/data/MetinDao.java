package com.example.hadi_bakalm.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.example.hadi_bakalm.model.MetinItem;

import java.util.List;

@Dao
public interface MetinDao {

    // Yeni kişisel metin ekleme
    @Insert
    void insert(MetinItem metinItem);

    // Kişisel notu veya kaydetme durumunu güncelleme
    @Update
    void update(MetinItem metinItem);

    // Tüm kişisel metinleri getirme
    @Query("SELECT * FROM metinler")
    List<MetinItem> getAllMetinler();

    // Sadece kaydedilen kişisel metinleri getirme
    @Query("SELECT * FROM metinler WHERE isSaved = 1")
    List<MetinItem> getSavedMetinler();

    // ID'ye göre tek bir metni getirme
    @Query("SELECT * FROM metinler WHERE id = :metinId LIMIT 1")
    MetinItem getMetinById(int metinId);
}