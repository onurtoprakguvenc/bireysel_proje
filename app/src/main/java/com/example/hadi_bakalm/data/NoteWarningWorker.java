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

    private static final String CHANNEL_ID = "gecici_not_uyari_kanali";

    public NoteWarningWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String notBaslik = getInputData().getString("not_baslik");
        if (notBaslik == null || notBaslik.trim().isEmpty()) {
            notBaslik = "Geçici Not";
        }

        sendNotification(notBaslik);
        return Result.success();
    }

    private void sendNotification(String notBaslik) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 (API 26) ve üzeri için Bildirim Kanalı oluşturulması
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geçici Not Uyarıları",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Geçici notların silinmesine 10 dakika kala uyarı bildirimi gönderir.");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_time) // Projenizdeki saat/uyarı ikonu
                .setContentTitle("Notunuz Silinmek Üzere")
                .setContentText("\"" + notBaslik + "\" başlıklı notunuz yaklaşık 10 dakika içinde silinecektir.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }
}