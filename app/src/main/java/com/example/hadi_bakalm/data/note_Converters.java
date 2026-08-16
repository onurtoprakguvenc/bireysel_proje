package com.example.hadi_bakalm.data;

import androidx.room.TypeConverter;
import com.example.hadi_bakalm.model.NoteBlockModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class note_Converters {

    private static final Gson GSON = new Gson();
    private static final Type BLOCK_LIST_TYPE = new TypeToken<List<NoteBlockModel>>() {}.getType();

    @TypeConverter
    public static String fromBlockList(List<NoteBlockModel> blocks) {
        if (blocks == null) return null;
        return GSON.toJson(blocks);
    }

    @TypeConverter
    public static List<NoteBlockModel> toBlockList(String blocksJson) {
        if (blocksJson == null || blocksJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return GSON.fromJson(blocksJson, BLOCK_LIST_TYPE);
    }
}