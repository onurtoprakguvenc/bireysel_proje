package com.example.hadi_bakalm.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.hadi_bakalm.model.NoteBlockModel;

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

    // YENİ EKLENEN BLOK LİSTESİ ALANI
    public List<NoteBlockModel> blocks;

    public notentity(String title, String content, String category, String colorHex, String timestamp) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.colorHex = colorHex;
        this.timestamp = timestamp;
        this.isPinned = false;
    }
}