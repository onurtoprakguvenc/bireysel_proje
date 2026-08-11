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

    public NoteBlockModel(BlockType type) {
        this.type = type;
        this.content = "";
    }

    public NoteBlockModel(BlockType type, String content) {
        this.type = type;
        this.content = content;
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
}