package com.example.hadi_bakalm.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// 1. Versiyonu 1 artırın (Örn: version = 2)
@Database(entities = {notentity.class}, version = 2, exportSchema = false)
@TypeConverters({note_Converters.class})
public abstract class not_app_database extends RoomDatabase {

    private static volatile not_app_database INSTANCE;

    public abstract notdao noteDao();

    public static not_app_database getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (not_app_database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    not_app_database.class,
                                    "not_veritabani"
                            )
                            // Sürüm yükseltmelerinde notlar SİLİNMEZ: şema değiştiğinde
                            // version artırılmalı ve .addMigrations(...) ile Migration eklenmelidir.
                            // Sadece sürüm düşürmede (downgrade) veritabanı sıfırlanır.
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}