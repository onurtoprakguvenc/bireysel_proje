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

    public notentity(String title, String content, String category, String colorHex, String timestamp) {
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.category = category != null ? category : "Genel";
        this.colorHex = colorHex != null ? colorHex : "#FFFFFF";
        this.timestamp = timestamp != null ? timestamp : "";
        this.isPinned = false;
        this.blocks = new ArrayList<>();
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