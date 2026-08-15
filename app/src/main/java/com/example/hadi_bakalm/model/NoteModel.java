package com.example.hadi_bakalm.model;

@SuppressWarnings("unused")
public class NoteModel {
    private int id; // Veritabanı ID'si
    private final String title;
    private final String content;
    private final String date;
    private final String category;
    private final boolean isPinned;

    // 1. Standart/Eski Oluşturmalar İçin Kurucu Metot
    public NoteModel(String title, String content, String date, String category, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.category = category;
        this.isPinned = isPinned;
    }

    // 2. Veritabanından Okurken ID Dahil Eden Kurucu Metot
    public NoteModel(int id, String title, String content, String date, String category, boolean isPinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.category = category;
        this.isPinned = isPinned;
    }

    // Getter ve Setter Metotları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public boolean isPinned() { return isPinned; }
}