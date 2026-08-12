package com.example.hadi_bakalm.model;

public class NoteBlockModel {

    public enum BlockType {
        TEXT,
        DRAWING,
        TABLE,
        VOICE
    }

    private BlockType type;
    private String content; // Metin bloğu için içerik

    // Tablo bloğu için satır ve sütun değişkenleri (YENİ EKLENEN)
    private int rows = 3;
    private int cols = 3;

    public NoteBlockModel(BlockType type) {
        this.type = type;
        this.content = "";
    }

    public NoteBlockModel(BlockType type, String content) {
        this.type = type;
        this.content = content;
    }

    // Tablo bloğu oluştururken satır ve sütun alan kurucu metot (YENİ EKLENEN)
    public NoteBlockModel(BlockType type, int rows, int cols) {
        this.type = type;
        this.content = "";
        this.rows = rows;
        this.cols = cols;
    }

    public BlockType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Tablo için Getter/Setter metotları (YENİ EKLENEN)
    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }
}