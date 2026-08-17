package com.example.hadi_bakalm.model;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressWarnings("unused")
public final class ThemeHelper {

    private static final String PREF_NAME = "ThemePrefs";
    private static final String KEY_IS_DARK_MODE = "isDarkMode";

    private ThemeHelper() {
        throw new UnsupportedOperationException("ThemeHelper nesneleştirilemez.");
    }

    private static SharedPreferences getPrefs(Context context) {
        if (context == null) return null;
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Uygulama her açıldığında kaydedilen temayı uygular
    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) return;

        boolean isDarkMode = prefs.getBoolean(KEY_IS_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    // Temayı değiştirir (Aydınlık <-> Karanlık) ve SharedPreferences'a kaydeder
    public static void setDarkMode(Context context, boolean enableDarkMode) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) return;

        prefs.edit().putBoolean(KEY_IS_DARK_MODE, enableDarkMode).apply();

        AppCompatDelegate.setDefaultNightMode(
                enableDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    // Şu an karanlık modda olunup olunmadığını döndürür
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) return false;
        return prefs.getBoolean(KEY_IS_DARK_MODE, false);
    }
}