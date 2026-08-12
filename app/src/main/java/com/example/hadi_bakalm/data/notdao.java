package com.example.hadi_bakalm.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface notdao {

    // Tüm notları son güncellenene göre getir
    @Query("SELECT * FROM user_notes ORDER BY isPinned DESC, id DESC")
    List<notentity> getAllNotes();

    // ID'ye göre tek not getiren metod
    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    notentity getNoteById(int id);

    // Arama Çubuğu İçin Sorgu (Başlıkta veya İçerikte Ara)
    @Query("SELECT * FROM user_notes WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    List<notentity> searchNotes(String searchQuery);

    // Yeni Not Ekle veya Varsa Üzerine Yaz
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(notentity note);

    @Update
    void updateNote(notentity note);

    @Delete
    void deleteNote(notentity note);
}