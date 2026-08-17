package com.example.hadi_bakalm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.hadi_bakalm.model.CrashActivity;

public class App extends Application {

    private static final String PREF_NAME = "AyarlarPrefs";
    private static final String KEY_THEME_POSITION = "secilen_tema_pozisyon";

    @Override
    public void onCreate() {
        super.onCreate();

        setupGlobalCrashHandler();
        applyAppTheme();
    }

    private void setupGlobalCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String errorLog = Log.getStackTraceString(throwable);

            Intent intent = new Intent(this, CrashActivity.class);
            intent.putExtra("EXTRA_ERROR_LOG", errorLog);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });
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