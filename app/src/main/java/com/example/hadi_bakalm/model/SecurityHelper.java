package com.example.hadi_bakalm.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityHelper {

    private static final String PREF_NAME = "AppSecurityVaultPrefs";
    private static final String KEY_PASSWORD_HASH = "key_password_hash";
    private static final String KEY_PASSWORD_SALT = "key_password_salt";
    private static final String KEY_SECURITY_QUESTION = "key_sec_question";
    private static final String KEY_SECURITY_ANSWER_HASH = "key_sec_answer_hash";
    private static final String KEY_SECURITY_ANSWER_SALT = "key_sec_answer_salt";

    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int PBKDF2_KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isPasswordSet(Context context) {
        return getPrefs(context).contains(KEY_PASSWORD_HASH);
    }

    public static void setPasswordAndQuestion(Context context, String password, String question, String answer) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        putSecret(editor, KEY_PASSWORD_HASH, KEY_PASSWORD_SALT, normalizePassword(password));
        editor.putString(KEY_SECURITY_QUESTION, question.trim());
        putSecret(editor, KEY_SECURITY_ANSWER_HASH, KEY_SECURITY_ANSWER_SALT, normalizeAnswer(answer));
        editor.apply();
    }

    public static boolean checkPassword(Context context, String password) {
        if (password == null) return false;
        return checkSecret(context, KEY_PASSWORD_HASH, KEY_PASSWORD_SALT, normalizePassword(password));
    }

    public static String getSecurityQuestion(Context context) {
        return getPrefs(context).getString(KEY_SECURITY_QUESTION, "Güvenlik sorusu bulunamadı.");
    }

    public static boolean checkSecurityAnswer(Context context, String answer) {
        if (answer == null) return false;
        return checkSecret(context, KEY_SECURITY_ANSWER_HASH, KEY_SECURITY_ANSWER_SALT, normalizeAnswer(answer));
    }

    public static void resetPassword(Context context, String newPassword) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        putSecret(editor, KEY_PASSWORD_HASH, KEY_PASSWORD_SALT, normalizePassword(newPassword));
        editor.apply();
    }

    private static String normalizePassword(String password) {
        return password.trim();
    }

    private static String normalizeAnswer(String answer) {
        return answer.trim().toLowerCase();
    }

    private static void putSecret(SharedPreferences.Editor editor, String hashKey, String saltKey, String secret) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        editor.putString(saltKey, Base64.encodeToString(salt, Base64.NO_WRAP));
        editor.putString(hashKey, pbkdf2(secret, salt));
    }

    private static boolean checkSecret(Context context, String hashKey, String saltKey, String secret) {
        SharedPreferences prefs = getPrefs(context);
        String savedHash = prefs.getString(hashKey, null);
        if (savedHash == null || savedHash.isEmpty()) return false;

        String savedSalt = prefs.getString(saltKey, null);
        if (savedSalt != null) {
            String computed = pbkdf2(secret, Base64.decode(savedSalt, Base64.NO_WRAP));
            return computed != null && MessageDigest.isEqual(
                    savedHash.getBytes(StandardCharsets.UTF_8), computed.getBytes(StandardCharsets.UTF_8));
        }

        // Eski sürüm: tuzsuz SHA-256. Doğrulanırsa yeni biçime yükseltilir.
        String legacy = legacySha256(secret);
        boolean matches = legacy != null && MessageDigest.isEqual(
                savedHash.getBytes(StandardCharsets.UTF_8), legacy.getBytes(StandardCharsets.UTF_8));
        if (matches) {
            SharedPreferences.Editor editor = prefs.edit();
            putSecret(editor, hashKey, saltKey, secret);
            editor.apply();
        }
        return matches;
    }

    private static String pbkdf2(String secret, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH_BITS);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private static String legacySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
