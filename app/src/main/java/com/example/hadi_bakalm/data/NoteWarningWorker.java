package com.example.hadi_bakalm.data;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.hadi_bakalm.R;

public class NoteWarningWorker extends Worker {

    public static final String KEY_NOTE_ID = "not_id";
    public static final String KEY_EXPIRE_TIMESTAMP = "not_bitis_zamani";
    public static final String KEY_NOTE_TITLE = "not_baslik";
    public static final String KEY_WARNING_TEXT = "uyari_metni";

    private static final String CHANNEL_ID = "gecici_not_uyari_kanali";

    public NoteWarningWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String notBaslik = getInputData().getString(KEY_NOTE_TITLE);
        String uyariMetni = getInputData().getString(KEY_WARNING_TEXT);

        if (notBaslik == null || notBaslik.trim().isEmpty()) {
            notBaslik = "Geçici Not";
        }
        if (uyariMetni == null || uyariMetni.trim().isEmpty()) {
            uyariMetni = "\"" + notBaslik + "\" başlıklı notunuz silinmek üzere.";
        }

        if (!isWarningStillValid()) {
            return Result.success();
        }

        sendNotification(uyariMetni);
        return Result.success();
    }

    // Not bu arada silindi, geri yüklendi (geçicilikten çıktı) veya süresi değiştiyse uyarı gönderilmez
    private boolean isWarningStillValid() {
        int noteId = getInputData().getInt(KEY_NOTE_ID, -1);
        if (noteId == -1) return true; // Eski sürümden kalan görevler: kontrol bilgisi yok

        long expectedExpire = getInputData().getLong(KEY_EXPIRE_TIMESTAMP, 0L);
        try {
            notentity note = not_app_database.getInstance(getApplicationContext()).noteDao().getNoteById(noteId);
            return note != null
                    && !note.isInTrash
                    && note.isEphemeral
                    && note.expireTimestamp == expectedExpire;
        } catch (Exception e) {
            return true;
        }
    }

    private void sendNotification(String uyariMetni) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 (API 26) ve üzeri için Bildirim Kanalı oluşturulması
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geçici Not Uyarıları",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Geçici notların silinmesine az süre kala uyarı bildirimi gönderir.");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_time)
                .setContentTitle("Notunuz Silinmek Üzere")
                .setContentText(uyariMetni)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(uyariMetni))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }
}
