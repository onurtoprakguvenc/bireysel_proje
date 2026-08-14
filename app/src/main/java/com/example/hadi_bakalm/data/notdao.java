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

    // Arama sonuçlarında da sabitlenenler üstte
    @Query("SELECT * FROM user_notes WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY isPinned DESC, id DESC")
    List<notentity> searchNotes(String searchQuery);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(notentity note);

    @Update
    void updateNote(notentity note);

    @Delete
    void deleteNote(notentity note);
}