package com.example.hadi_bakalm.model;

public class kaydedilenler {
    private String id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String addedTime;
    private boolean isSaved; // Eklenen değişken

    // Boş Constructor
    public kaydedilenler() {
    }

    // Parametreli Constructor (isSaved olmadan kullanım için varsayılan false)
    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.addedTime = addedTime;
        this.isSaved = false;
    }

    // Tam Parametreli Constructor
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(String addedTime) {
        this.addedTime = addedTime;
    }

    // Hatanın çözümü için gerekli metod:
    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}