package com.example.hadi_bakalm.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {notentity.class}, version = 1, exportSchema = false)
public abstract class not_app_database extends RoomDatabase {

    private static not_app_database instance;

    public abstract notdao noteDao();

    public static synchronized not_app_database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            not_app_database.class,
                            "sade_not_database"
                    ).allowMainThreadQueries() // Basit testler ve hızlı senkronizasyon için
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}