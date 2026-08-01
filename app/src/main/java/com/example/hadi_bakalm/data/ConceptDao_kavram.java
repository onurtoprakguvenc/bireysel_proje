package com.example.hadi_bakalm.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.hadi_bakalm.model.ConceptItem_kavram;

import java.util.List;

@Dao
public interface ConceptDao_kavram {

    @Insert
    long insert(ConceptItem_kavram conceptItem);

    @Update
    void update(ConceptItem_kavram conceptItem);

    // --- SİLME METOTLARI ---
    @Delete
    void delete(ConceptItem_kavram conceptItem); // Tek bir ögeyi silmek için

    @Query("DELETE FROM kavramlar")
    void deleteAll(); // Tüm geçmişi/kayıtları tek hamlede silmek için
    // ---------------------------------

    @Query("SELECT * FROM kavramlar")
    List<ConceptItem_kavram> getAllConceptler();

    @Query("SELECT * FROM kavramlar WHERE isSaved = 1")
    List<ConceptItem_kavram> getSavedConceptler();

    @Query("SELECT * FROM kavramlar WHERE id = :conceptId LIMIT 1")
    ConceptItem_kavram getConceptById(int conceptId);
}