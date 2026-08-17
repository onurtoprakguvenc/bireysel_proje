package com.example.hadi_bakalm.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.hadi_bakalm.model.NoteBlockModel;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "user_notes")
public class notentity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String content;
    public String category;
    public String colorHex;
    public String timestamp;
    public boolean isPinned;

    public String imagePath;
    public String drawingBlobPath;
    public String voiceNotePath;
    public String tableJsonData;

    public List<NoteBlockModel> blocks;

    // --- GEÇİCİ NOT VE GERİ DÖNÜŞÜM KUTUSU ALANLARI ---
    public boolean isEphemeral;       // Notun geçici olup olmadığını belirten bayrak
    public long expireTimestamp;       // Notun süresinin dolup çöpe taşınacağı zaman damgası (milisaniye)
    public boolean isInTrash;          // Notun çöp kutusunda olup olmadığını belirten bayrak
    public long trashedTimestamp;      // Notun çöp kutusuna girdiği anın zaman damgası (milisaniye)

    public notentity(String title, String content, String category, String colorHex, String timestamp) {
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.category = category != null ? category : "Genel";
        this.colorHex = colorHex != null ? colorHex : "#FFFFFF";
        this.timestamp = timestamp != null ? timestamp : "";
        this.isPinned = false;
        this.blocks = new ArrayList<>();

        // Varsayılan geçici not değerleri (Kalıcı not)
        this.isEphemeral = false;
        this.expireTimestamp = 0L;
        this.isInTrash = false;
        this.trashedTimestamp = 0L;
    }

    @Ignore
    @SuppressWarnings("unused")
    public notentity(String title, String content, String category, String colorHex, String timestamp, List<NoteBlockModel> blocks) {
        this(title, content, category, colorHex, timestamp);
        if (blocks != null) {
            this.blocks = blocks;
        }
    }
}