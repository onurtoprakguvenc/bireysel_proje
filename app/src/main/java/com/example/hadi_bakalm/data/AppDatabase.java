package com.example.hadi_bakalm.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.hadi_bakalm.model.ConceptItem_kavram;
import com.example.hadi_bakalm.model.MetinItem;

@Database(entities = {MetinItem.class, ConceptItem_kavram.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract MetinDao metinDao();
    public abstract ConceptDao_kavram conceptDao_kavram();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "hadi_bakalim_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}