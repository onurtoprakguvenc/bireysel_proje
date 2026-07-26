package com.example.hadi_bakalm.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "metinler")
public class MetinItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String personalNote;
    private boolean isSaved;

    // Constructor (Yapıcı Metot)
    public MetinItem(String title, String content, String personalNote, boolean isSaved) {
        this.title = title;
        this.content = content;
        this.personalNote = personalNote;
        this.isSaved = isSaved;
    }

    // Getter ve Setter Metotları
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPersonalNote() {
        return personalNote;
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = personalNote;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}