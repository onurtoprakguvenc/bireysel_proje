package com.example.hadi_bakalm.model;

public class kaydedilenler {
    private String id;
    private String title;
    private String description;
    private String type; // "METİN" veya "KAVRAM"
    private String category; // "Kişisel Not", "Karar Teorisi" vb.
    private String addedTime; // "Dün eklendi", "3 gün önce eklendi" vb.

    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.addedTime = addedTime;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getAddedTime() { return addedTime; }
}