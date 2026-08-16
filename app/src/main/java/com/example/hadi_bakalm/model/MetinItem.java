package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "metinler")
@SuppressWarnings("unused")
public class MetinItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String personalNote;
    private boolean isSaved;
    private long lastViewedTime;

    // Room'un varsayılan olarak kullanacağı ana constructor
    public MetinItem(String title, String content, String personalNote, boolean isSaved) {
        this.title = (title != null) ? title : "";
        this.content = (content != null) ? content : "";
        this.personalNote = (personalNote != null) ? personalNote : "";
        this.isSaved = isSaved;
        this.lastViewedTime = 0L;
    }

    // Zaman damgasıyla doğrudan nesne üretmek için alternatif constructor
    @Ignore
    public MetinItem(String title, String content, String personalNote, boolean isSaved, long lastViewedTime) {
        this(title, content, personalNote, isSaved);
        this.lastViewedTime = lastViewedTime;
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

    public void setTitle(String title) {
        this.title = (title != null) ? title : "";
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    public String getPersonalNote() {
        return personalNote != null ? personalNote : "";
    }

    public void setPersonalNote(String personalNote) {
        this.personalNote = (personalNote != null) ? personalNote : "";
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public long getLastViewedTime() {
        return lastViewedTime;
    }

    public void setLastViewedTime(long lastViewedTime) {
        this.lastViewedTime = lastViewedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetinItem metinItem = (MetinItem) o;
        return id == metinItem.id &&
                isSaved == metinItem.isSaved &&
                lastViewedTime == metinItem.lastViewedTime &&
                Objects.equals(title, metinItem.title) &&
                Objects.equals(content, metinItem.content) &&
                Objects.equals(personalNote, metinItem.personalNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, personalNote, isSaved, lastViewedTime);
    }

    @NonNull
    @Override
    public String toString() {
        return "MetinItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isSaved=" + isSaved +
                ", lastViewedTime=" + lastViewedTime +
                '}';
    }
}