package com.example.hadi_bakalm.model;

public class kaydedilenler {
    private String id;
    private String title;
    private String description;
    private String content; // EKLENDİ: Sadece değişken olarak eklendi
    private String type;
    private String category;
    private String addedTime;
    private boolean isSaved;

    // Boş Constructor
    public kaydedilenler() {
    }

    // Mevcut 6 Parametreli Constructor (DOKUNULMADI)
    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.addedTime = addedTime;
        this.isSaved = false;
    }

    // Mevcut 7 Parametreli Constructor (DOKUNULMADI - DİĞER SAYFALAR BOZULMAZ)
    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime, boolean isSaved) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.addedTime = addedTime;
        this.isSaved = isSaved;
    }

    // Getter ve Setter Metodları
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // EKLENDİ: Sadece Getter ve Setter
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddedTime() { return addedTime; }
    public void setAddedTime(String addedTime) { this.addedTime = addedTime; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }
}