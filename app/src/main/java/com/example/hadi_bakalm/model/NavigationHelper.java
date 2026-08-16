package com.example.hadi_bakalm.model;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;

public final class NavigationHelper {

    // Yardımcı sınıfın nesneleştirilmesini (instantiation) engellemek için private constructor
    private NavigationHelper() {
        throw new UnsupportedOperationException("Bu yardımcı sınıf nesneleştirilemez.");
    }

    public static void setupBottomNavigation(Activity currentActivity) {
        if (currentActivity == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
            return;
        }

        LinearLayout navCategories = currentActivity.findViewById(R.id.navCategories);
        LinearLayout navSaved = currentActivity.findViewById(R.id.navSaved);
        LinearLayout navRecent = currentActivity.findViewById(R.id.navRecent);
        LinearLayout navSettings = currentActivity.findViewById(R.id.navSettings);

        if (navCategories != null && !(currentActivity instanceof EskiMainActivity)) {
            navCategories.setOnClickListener(v -> navigateTo(currentActivity, EskiMainActivity.class));
        }

        if (navSaved != null && !(currentActivity instanceof kaydet_ana_sayfa)) {
            navSaved.setOnClickListener(v -> navigateTo(currentActivity, kaydet_ana_sayfa.class));
        }

        if (navRecent != null && !(currentActivity instanceof SonIncelemeActivity)) {
            navRecent.setOnClickListener(v -> navigateTo(currentActivity, SonIncelemeActivity.class));
        }

        if (navSettings != null && !(currentActivity instanceof AyarlarActivity)) {
            navSettings.setOnClickListener(v -> navigateTo(currentActivity, AyarlarActivity.class));
        }
    }

    private static void navigateTo(Activity currentActivity, Class<?> targetClass) {
        if (currentActivity == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
            return;
        }

        Intent intent = new Intent(currentActivity, targetClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(intent);
        currentActivity.finish();
        currentActivity.overridePendingTransition(0, 0);
    }
}