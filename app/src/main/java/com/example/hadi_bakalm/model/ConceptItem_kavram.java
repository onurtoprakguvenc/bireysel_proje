package com.example.hadi_bakalm.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "kavramlar")
public class ConceptItem_kavram {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;               // Kavram adı / temsili
    private String description;         // Normal açıklama
    private String developerNote;       // Geliştirici notu/düşüncesi
    private String exampleDialogues;    // Örnek diyaloglar
    private String practicalImportance; // Pratik hayattaki önemi
    private boolean isSaved;            // Kaydedildi mi?

    public ConceptItem_kavram(String title, String description, String developerNote,
                       String exampleDialogues, String practicalImportance, boolean isSaved) {
        this.title = title;
        this.description = description;
        this.developerNote = developerNote;
        this.exampleDialogues = exampleDialogues;
        this.practicalImportance = practicalImportance;
        this.isSaved = isSaved;
    }

    // Getter ve Setter Metotları
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDeveloperNote() { return developerNote; }
    public void setDeveloperNote(String developerNote) { this.developerNote = developerNote; }

    public String getExampleDialogues() { return exampleDialogues; }
    public void setExampleDialogues(String exampleDialogues) { this.exampleDialogues = exampleDialogues; }

    public String getPracticalImportance() { return practicalImportance; }
    public void setPracticalImportance(String practicalImportance) { this.practicalImportance = practicalImportance; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }
}