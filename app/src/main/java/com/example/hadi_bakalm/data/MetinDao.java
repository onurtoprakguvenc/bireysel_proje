package com.example.hadi_bakalm.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.hadi_bakalm.model.MetinItem;

import java.util.List;

@Dao
@SuppressWarnings("unused")
public interface MetinDao {

    @Insert
    void insert(MetinItem metinItem);

    @Update
    void update(MetinItem metinItem);

    @Query("SELECT * FROM metinler")
    List<MetinItem> getAllMetinler();

    @Query("SELECT * FROM metinler WHERE isSaved = 1")
    List<MetinItem> getSavedMetinler();

    @Query("SELECT * FROM metinler WHERE lastViewedTime > 0 ORDER BY lastViewedTime DESC")
    List<MetinItem> getRecentMetinler();

    @Query("SELECT * FROM metinler WHERE id = :metinId LIMIT 1")
    MetinItem getMetinById(int metinId);

    @Query("DELETE FROM metinler")
    void deleteAll();
}