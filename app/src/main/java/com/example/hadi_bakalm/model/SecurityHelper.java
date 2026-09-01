package com.example.hadi_bakalm.model;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;

public class SecurityHelper {

    private static final String PREF_NAME = "AppSecurityVaultPrefs";
    private static final String KEY_PASSWORD_HASH = "key_password_hash";
    private static final String KEY_SECURITY_QUESTION = "key_sec_question";
    private static final String KEY_SECURITY_ANSWER_HASH = "key_sec_answer_hash";

    public static boolean isPasswordSet(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_PASSWORD_HASH);
    }

    public static void setPasswordAndQuestion(Context context, String password, String question, String answer) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_PASSWORD_HASH, hashString(password.trim()));
        editor.putString(KEY_SECURITY_QUESTION, question.trim());
        editor.putString(KEY_SECURITY_ANSWER_HASH, hashString(answer.trim().toLowerCase()));
        editor.apply();
    }

    public static boolean checkPassword(Context context, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedHash = prefs.getString(KEY_PASSWORD_HASH, "");
        return savedHash.equals(hashString(password.trim()));
    }

    public static String getSecurityQuestion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SECURITY_QUESTION, "Güvenlik sorusu bulunamadı.");
    }

    public static boolean checkSecurityAnswer(Context context, String answer) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedAnswerHash = prefs.getString(KEY_SECURITY_ANSWER_HASH, "");
        return savedAnswerHash.equals(hashString(answer.trim().toLowerCase()));
    }

    public static void resetPassword(Context context, String newPassword) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_PASSWORD_HASH, hashString(newPassword.trim()));
        editor.apply();
    }

    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }
}