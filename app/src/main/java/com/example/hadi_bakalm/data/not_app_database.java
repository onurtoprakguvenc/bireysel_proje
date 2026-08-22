package com.example.hadi_bakalm.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {notentity.class}, version =5, exportSchema = false)
@TypeConverters({note_Converters.class})
public abstract class not_app_database extends RoomDatabase {

    private static volatile not_app_database instance;

    public abstract notdao noteDao();

    public static not_app_database getInstance(Context context) {
        if (instance == null) {
            synchronized (not_app_database.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    not_app_database.class,
                                    "sade_not_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}