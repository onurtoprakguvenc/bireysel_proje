package com.example.hadi_bakalm.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_notes")
public class notentity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title; // Not Başlığı
    public String content; // Metin İçeriği
    public String category; // Etiket/Kategori (Kişisel, İş vb.)
    public String colorHex; // Selected Color from 7-color palette
    public String timestamp; // Oluşturulma/Güncellenme Zamanı
    public boolean isPinned; // Sabitlenme Durumu

    // Medya & Çizim Yolları
    public String imagePath; // Eklenen görselin cihaz içi dosya yolu
    public String drawingBlobPath;// Canvas çiziminin PNG olarak kaydedildiği yerel yol
    public String voiceNotePath; // Ses kaydının yerel dosya yolu
    public String tableJsonData; // 3x5 Tablo verisinin JSON string karşılığı

    public notentity(String title, String content, String category, String colorHex, String timestamp) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.colorHex = colorHex;
        this.timestamp = timestamp;
        this.isPinned = false;
    }
}
