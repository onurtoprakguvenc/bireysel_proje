package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import java.util.Objects;

@SuppressWarnings("unused")
public class NoteModel {

    private int id;
    private final String title;
    private final String content;
    private final String date;
    private final String category;
    private final boolean isPinned;

    // 1. Standart Oluşturmalar İçin Kurucu Metot
    public NoteModel(String title, String content, String date, String category, boolean isPinned) {
        this(0, title, content, date, category, isPinned);
    }

    // 2. Veritabanından Okurken ID Dahil Eden Kurucu Metot
    public NoteModel(int id, String title, String content, String date, String category, boolean isPinned) {
        this.id = id;
        this.title = (title != null) ? title : "";
        this.content = (content != null) ? content : "";
        this.date = (date != null) ? date : "";
        this.category = (category != null) ? category : "";
        this.isPinned = isPinned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public boolean isPinned() {
        return isPinned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteModel noteModel = (NoteModel) o;
        return id == noteModel.id &&
                isPinned == noteModel.isPinned &&
                Objects.equals(title, noteModel.title) &&
                Objects.equals(content, noteModel.content) &&
                Objects.equals(date, noteModel.date) &&
                Objects.equals(category, noteModel.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, date, category, isPinned);
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", isPinned=" + isPinned +
                '}';
    }
}