package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "kavramlar")
@SuppressWarnings("unused")
public class ConceptItem_kavram {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private String developerNote;
    private String exampleDialogues;
    private String practicalImportance;
    private boolean isSaved;
    private long lastViewedTime;

    // Room'un kullanacağı ana constructor
    public ConceptItem_kavram(String title, String description, String developerNote,
                              String exampleDialogues, String practicalImportance, boolean isSaved) {
        this.title = (title != null) ? title : "";
        this.description = (description != null) ? description : "";
        this.developerNote = (developerNote != null) ? developerNote : "";
        this.exampleDialogues = (exampleDialogues != null) ? exampleDialogues : "";
        this.practicalImportance = (practicalImportance != null) ? practicalImportance : "";
        this.isSaved = isSaved;
        this.lastViewedTime = 0L;
    }

    // Kod içinden zaman damgasıyla doğrudan nesne oluşturmak için alternatif constructor
    @Ignore
    public ConceptItem_kavram(String title, String description, String developerNote,
                              String exampleDialogues, String practicalImportance, boolean isSaved, long lastViewedTime) {
        this(title, description, developerNote, exampleDialogues, practicalImportance, isSaved);
        this.lastViewedTime = lastViewedTime;
    }

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
        this.title = (title != null) ? title : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description : "";
    }

    public String getDeveloperNote() {
        return developerNote;
    }

    public void setDeveloperNote(String developerNote) {
        this.developerNote = (developerNote != null) ? developerNote : "";
    }

    public long getLastViewedTime() {
        return lastViewedTime;
    }

    public void setLastViewedTime(long lastViewedTime) {
        this.lastViewedTime = lastViewedTime;
    }

    public String getExampleDialogues() {
        return exampleDialogues;
    }

    public void setExampleDialogues(String exampleDialogues) {
        this.exampleDialogues = (exampleDialogues != null) ? exampleDialogues : "";
    }

    public String getPracticalImportance() {
        return practicalImportance;
    }

    public void setPracticalImportance(String practicalImportance) {
        this.practicalImportance = (practicalImportance != null) ? practicalImportance : "";
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
        ConceptItem_kavram that = (ConceptItem_kavram) o;
        return id == that.id &&
                isSaved == that.isSaved &&
                lastViewedTime == that.lastViewedTime &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(developerNote, that.developerNote) &&
                Objects.equals(exampleDialogues, that.exampleDialogues) &&
                Objects.equals(practicalImportance, that.practicalImportance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, developerNote, exampleDialogues, practicalImportance, isSaved, lastViewedTime);
    }

    @NonNull
    @Override
    public String toString() {
        return "ConceptItem_kavram{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isSaved=" + isSaved +
                ", lastViewedTime=" + lastViewedTime +
                '}';
    }
}