package com.example.hadi_bakalm;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Uygulama ilk başlatıldığında tema ayarını hafızadan tek merkezde yükle
        SharedPreferences prefs = getSharedPreferences("AyarlarPrefs", Context.MODE_PRIVATE);
        int position = prefs.getInt("secilen_tema_pozisyon", 0);

        switch (position) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}