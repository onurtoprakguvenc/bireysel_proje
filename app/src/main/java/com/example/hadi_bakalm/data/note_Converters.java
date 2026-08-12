package com.example.hadi_bakalm.data;

import androidx.room.TypeConverter;
import com.example.hadi_bakalm.model.NoteBlockModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class note_Converters {

    @TypeConverter
    public static String fromBlockList(List<NoteBlockModel> blocks) {
        if (blocks == null) return null;
        Gson gson = new Gson();
        return gson.toJson(blocks);
    }

    @TypeConverter
    public static List<NoteBlockModel> toBlockList(String blocksJson) {
        if (blocksJson == null) return null;
        Gson gson = new Gson();
        // DÜZELTİLEN SATIR: TypeToken içindeki > sembolü teke düşürüldü
        Type type = new TypeToken<List<NoteBlockModel>>() {}.getType();
        return gson.fromJson(blocksJson, type);
    }
}