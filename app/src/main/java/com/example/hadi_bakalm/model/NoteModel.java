package com.example.hadi_bakalm.model;

public class NoteModel {
    private String title;
    private String content;
    private String date;
    private String category;
    private boolean isPinned;

    public NoteModel(String title, String content, String date, String category, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.category = category;
        this.isPinned = isPinned;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public boolean isPinned() { return isPinned; }
}