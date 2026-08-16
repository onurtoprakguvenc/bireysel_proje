package com.example.hadi_bakalm.model;

import androidx.annotation.NonNull;
import java.util.Objects;

@SuppressWarnings("unused")
public class NoteBlockModel {

    public enum BlockType {
        TEXT,
        DRAWING,
        TABLE,
        VOICE
    }

    private final BlockType type;
    private String content;
    private int rows;
    private int cols;

    public NoteBlockModel(BlockType type) {
        this(type, "", 3, 3);
    }

    public NoteBlockModel(BlockType type, String content) {
        this(type, content, 3, 3);
    }

    public NoteBlockModel(BlockType type, int rows, int cols) {
        this(type, "", rows, cols);
    }

    public NoteBlockModel(BlockType type, String content, int rows, int cols) {
        this.type = (type != null) ? type : BlockType.TEXT;
        this.content = (content != null) ? content : "";
        this.rows = Math.max(1, rows);
        this.cols = Math.max(1, cols);
    }

    public BlockType getType() {
        return type;
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = Math.max(1, rows);
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = Math.max(1, cols);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteBlockModel that = (NoteBlockModel) o;
        return rows == that.rows &&
                cols == that.cols &&
                type == that.type &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content, rows, cols);
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteBlockModel{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", rows=" + rows +
                ", cols=" + cols +
                '}';
    }
}