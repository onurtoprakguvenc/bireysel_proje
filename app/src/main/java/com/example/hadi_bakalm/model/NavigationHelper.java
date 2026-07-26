package com.example.hadi_bakalm.model;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;

import com.example.hadi_bakalm.MainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.AyarlarActivity;
import com.example.hadi_bakalm.model.KisiselMetinlerimActivity;
import com.example.hadi_bakalm.model.SonIncelemeActivity;
import com.example.hadi_bakalm.model.kaydet_ana_sayfa;

public class NavigationHelper {

    public static void setupBottomNavigation(Activity currentActivity) {
        LinearLayout navCategories = currentActivity.findViewById(R.id.navCategories);
        LinearLayout navSaved = currentActivity.findViewById(R.id.navSaved);
        LinearLayout navRecent = currentActivity.findViewById(R.id.navRecent);
        LinearLayout navSettings = currentActivity.findViewById(R.id.navSettings);

        if (navCategories != null && !(currentActivity instanceof MainActivity)) {
            navCategories.setOnClickListener(v -> navigateTo(currentActivity, MainActivity.class));
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
        Intent intent = new Intent(currentActivity, targetClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(intent);
        currentActivity.finish();
        currentActivity.overridePendingTransition(0, 0);
    }
}

