package com.example.hadi_bakalm.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.hadi_bakalm.model.MetinItem;

@Database(entities = {MetinItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract MetinDao metinDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "hadi_bakalim_database"
                    ).allowMainThreadQueries() // Başlangıç testi için main thread izni
                    .build();
        }
        return instance;
    }
}