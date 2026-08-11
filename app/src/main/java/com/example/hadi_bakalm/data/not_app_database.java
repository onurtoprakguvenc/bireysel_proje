package com.example.hadi_bakalm.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {notentity.class}, version = 1, exportSchema = false)
public abstract class not_app_database extends RoomDatabase {

    private static AppDatabase instance;

    public abstract notdao noteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "sade_not_database" // Yerel Veritabanı Adı
                    ).allowMainThreadQueries() // Basit testler için (Daha sonra Coroutine/AsyncTask bağlanır)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}