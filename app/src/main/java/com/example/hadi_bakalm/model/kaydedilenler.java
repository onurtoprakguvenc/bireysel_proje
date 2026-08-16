package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;

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

    // Boş Constructor (Gson serileştirme/ayrıştırma için zorunlu)
    public kaydedilenler() {
        this.id = "";
        this.title = "";
        this.description = "";
        this.personalNote = "";
        this.dialogues = "";
        this.importance = "";
        this.type = "";
        this.category = "";
        this.addedTime = "";
        this.isSaved = false;
    }

    // 6 Parametreli Constructor
    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime) {
        this(id, title, description, type, category, addedTime, false);
    }

    // 7 Parametreli Constructor
    public kaydedilenler(String id, String title, String description, String type, String category, String addedTime, boolean isSaved) {
        this.id = (id != null) ? id : "";
        this.title = (title != null) ? title : "";
        this.description = (description != null) ? description : "";
        this.personalNote = "";
        this.dialogues = "";
        this.importance = "";
        this.type = (type != null) ? type : "";
        this.category = (category != null) ? category : "";
        this.addedTime = (addedTime != null) ? addedTime : "";
        this.isSaved = isSaved;
    }

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = (id != null) ? id : "";
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = (title != null) ? title : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description : "";
    }

    public String getPersonalNote() {
        return personalNote != null ? personalNote : "";
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = (personalNote != null) ? personalNote : "";
    }

    // Geriye dönük uyumluluk (getContent / setContent)
    public String getContent() {
        return getPersonalNote();
    }

    public void setContent(String content) {
        setPersonalNote(content);
    }

    public String getDialogues() {
        return dialogues != null ? dialogues : "";
    }

    public void setDialogues(String dialogues) {
        this.dialogues = (dialogues != null) ? dialogues : "";
    }

    public String getImportance() {
        return importance != null ? importance : "";
    }

    public void setImportance(String importance) {
        this.importance = (importance != null) ? importance : "";
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = (type != null) ? type : "";
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = (category != null) ? category : "";
    }

    public String getAddedTime() {
        return addedTime != null ? addedTime : "";
    }

    public void setAddedTime(String addedTime) {
        this.addedTime = (addedTime != null) ? addedTime : "";
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        kaydedilenler that = (kaydedilenler) o;
        return isSaved == that.isSaved &&
                Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(personalNote, that.personalNote) &&
                Objects.equals(type, that.type) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, personalNote, type, category, isSaved);
    }

    @NonNull
    @Override
    public String toString() {
        return "kaydedilenler{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", isSaved=" + isSaved +
                '}';
    }
}