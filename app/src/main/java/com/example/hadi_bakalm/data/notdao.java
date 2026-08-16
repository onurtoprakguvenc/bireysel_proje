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

    // Sabitlenenler en başta, ardından en son eklenenler
    @Query("SELECT * FROM user_notes ORDER BY isPinned DESC, id DESC")
    List<notentity> getAllNotes();

    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    notentity getNoteById(int id);

    // Kategoriye göre getirme (Veritabanı seviyesinde filtreleme)
    @Query("SELECT * FROM user_notes WHERE category = :category ORDER BY isPinned DESC, id DESC")
    List<notentity> getNotesByCategory(String category);

    // Arama sonuçlarında da sabitlenenler üstte
    @Query("SELECT * FROM user_notes WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY isPinned DESC, id DESC")
    List<notentity> searchNotes(String searchQuery);

    @Query("SELECT COUNT(*) FROM user_notes")
    int getNoteCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(notentity note);

    @Update
    void updateNote(notentity note);

    @Delete
    void deleteNote(notentity note);

    // Nesne oluşturmadan doğrudan ID ile hafif silme
    @Query("DELETE FROM user_notes WHERE id = :id")
    void deleteNoteById(int id);

    @Query("DELETE FROM user_notes")
    void deleteAllNotes();
}