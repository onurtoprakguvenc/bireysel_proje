package com.example.hadi_bakalm.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class kaydedilenler {
    private String id;
    private String title;
    private String description;

    @SerializedName(value = "personalNote", alternate = {"content"})
    private String personalNote;

    private String dialogues;
    private String importance;
    private String type;
    private String category;
    private String addedTime;
    private boolean isSaved;

    // Boş Constructor (Gson için)
    public kaydedilenler() {
    }

    // 6 Parametreli Constructor
    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.addedTime = addedTime;
        this.isSaved = false;
    }

    // 7 Parametreli Constructor
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

    public String getPersonalNote() { return personalNote; }
    public void setPersonalNote(String personalNote) { this.personalNote = personalNote; }

    // Eski koddaki bağımlılıklar için geriye dönük uyumluluk (getContent)
    public String getContent() { return personalNote; }
    public void setContent(String content) { this.personalNote = content; }

    public String getDialogues() { return dialogues; }
    public void setDialogues(String dialogues) { this.dialogues = dialogues; }

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddedTime() { return addedTime; }
    public void setAddedTime(String addedTime) { this.addedTime = addedTime; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }
}