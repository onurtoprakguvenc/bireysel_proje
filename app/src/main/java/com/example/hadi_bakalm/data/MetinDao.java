package com.example.hadi_bakalm.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.hadi_bakalm.model.MetinItem;

import java.util.List;

@Dao
public interface MetinDao {

    @Insert
    void insert(MetinItem metinItem);

    @Update
    void update(MetinItem metinItem);

    @Query("SELECT * FROM metinler")
    List<MetinItem> getAllMetinler();

    @Query("SELECT * FROM metinler WHERE isSaved = 1")
    List<MetinItem> getSavedMetinler();

    // DÜZELTME 1: Son incelenen metinler
    @Query("SELECT * FROM metinler WHERE lastViewedTime > 0 ORDER BY lastViewedTime DESC")
    List<MetinItem> getRecentMetinler();

    // DÜZELTME 2: Parametre 'int' olarak korundu (Room entity varsayılan int ID uyumu için)
    @Query("SELECT * FROM metinler WHERE id = :metinId LIMIT 1")
    MetinItem getMetinById(int metinId);

    // SILME METODU
    @Query("DELETE FROM metinler")
    void deleteAll();
}