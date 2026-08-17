package com.example.hadi_bakalm;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {

    private static final String PREF_NAME = "AyarlarPrefs";
    private static final String KEY_THEME_POSITION = "secilen_tema_pozisyon";

    @Override
    public void onCreate() {
        super.onCreate();
        applyAppTheme();
    }

    private void applyAppTheme() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int themePosition = prefs.getInt(KEY_THEME_POSITION, 0);

        int nightMode;
        switch (themePosition) {
            case 1:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case 2:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
            case 0:
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}