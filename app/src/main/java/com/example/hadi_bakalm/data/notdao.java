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

    // Ana Ekran: Sadece aktif (çöpte olmayan) notlar; sabitlenenler en başta, ardından en son eklenenler
    @Query("SELECT * FROM user_notes WHERE isInTrash = 0 ORDER BY isPinned DESC, id DESC")
    List<notentity> getAllNotes();

    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    notentity getNoteById(int id);

    // Kategoriye göre getirme (Sadece aktif notlar)
    @Query("SELECT * FROM user_notes WHERE category = :category AND isInTrash = 0 ORDER BY isPinned DESC, id DESC")
    List<notentity> getNotesByCategory(String category);

    // Arama sonuçları (Sadece aktif notlar içinde arar)
    @Query("SELECT * FROM user_notes WHERE (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%') AND isInTrash = 0 ORDER BY isPinned DESC, id DESC")
    List<notentity> searchNotes(String searchQuery);

    @Query("SELECT COUNT(*) FROM user_notes WHERE isInTrash = 0")
    int getNoteCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(notentity note);

    @Update
    void updateNote(notentity note);

    @Delete
    void deleteNote(notentity note);

    // Nesne oluşturmadan doğrudan ID ile silme
    @Query("DELETE FROM user_notes WHERE id = :id")
    void deleteNoteById(int id);

    @Query("DELETE FROM user_notes")
    void deleteAllNotes();

    // =========================================================================
    // GEÇİCİ NOT VE GERİ DÖNÜŞÜM KUTUSU SORGULARI
    // =========================================================================

    // Süresi dolan geçici notları Geri Dönüşüm Kutusuna taşı
    @Query("UPDATE user_notes SET isInTrash = 1, trashedTimestamp = :currentTime WHERE isEphemeral = 1 AND expireTimestamp <= :currentTime AND isInTrash = 0")
    void moveExpiredNotesToTrash(long currentTime);

    // Çöp kutusunda 7 günden fazla beklemiş notları kalıcı olarak tamamen sil
    @Query("DELETE FROM user_notes WHERE isInTrash = 1 AND trashedTimestamp <= :thresholdTime")
    void purgeOldDeletedNotes(long thresholdTime);

    // Geri Dönüşüm Kutusu ekranı için çöpteki notları listeleme
    @Query("SELECT * FROM user_notes WHERE isInTrash = 1 ORDER BY trashedTimestamp DESC")
    List<notentity> getTrashNotes();

    // Çöpteki bir notu geri yükleme (Geri Dönüşüm Kutusundan çıkarma)
    @Query("UPDATE user_notes SET isInTrash = 0, trashedTimestamp = 0, isEphemeral = 0, expireTimestamp = 0 WHERE id = :id")
    void restoreNoteFromTrash(int id);

    // Çöp kutusundaki tüm notları tek seferde kalıcı olarak temizleme
    @Query("DELETE FROM user_notes WHERE isInTrash = 1")
    void emptyTrash();

    // Notu silmeyip Geri Dönüşüm Kutusuna taşıma (Soft Delete)
    @Query("UPDATE user_notes SET isInTrash = 1, trashedTimestamp = :currentTime WHERE id = :id")
    void moveToTrash(int id, long currentTime);
}