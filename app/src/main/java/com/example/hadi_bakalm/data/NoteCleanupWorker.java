package com.example.hadi_bakalm.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NoteCleanupWorker extends Worker {

    private static final String TAG = "NoteCleanupWorker";
    private static final long SEVEN_DAYS_IN_MILLIS = 7L * 24 * 60 * 60 * 1000;

    public NoteCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Room veritabanı örneği ve DAO erişimi
            notdao noteDao = not_app_database.getInstance(getApplicationContext()).noteDao();
            long currentTime = System.currentTimeMillis();

            // 1. ADIM: Süresi dolmuş geçici notları Geri Dönüşüm Kutusuna taşı
            noteDao.moveExpiredNotesToTrash(currentTime);
            Log.d(TAG, "Süresi dolan geçici notlar çöp kutusuna taşındı. Zaman: " + currentTime);

            // 2. ADIM: Çöp kutusunda 7 günden fazla beklemiş notları tamamen yok et
            long purgeThreshold = currentTime - SEVEN_DAYS_IN_MILLIS;
            noteDao.purgeOldDeletedNotes(purgeThreshold);
            Log.d(TAG, "7 günü doldurmuş eski notlar kalıcı olarak silindi. Eşik: " + purgeThreshold);

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Temizlik görevi yürütülürken hata oluştu: ", e);
            return Result.retry();
        }
    }
}