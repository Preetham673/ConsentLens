package com.example.consentlens;

import android.app.NotificationChannel;
import com.example.consentlens.RiskAlertActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AppInstallReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ConsentLensChannel";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

            Uri data = intent.getData();
            if (data == null) return;

            String packageName = data.getSchemeSpecificPart();

            // Avoid detecting itself
            if (packageName.equals(context.getPackageName())) return;

            // Debug
            Toast.makeText(context,
                    "New app detected: " + packageName,
                    Toast.LENGTH_SHORT).show();

            showNotification(context, packageName);
        }
    }

    private void showNotification(Context context, String packageName) {

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ConsentLens Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Privacy risk alerts for newly installed apps");
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, RiskAlertActivity.class);
        intent.putExtra("packageName", packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                packageName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("New App Installed")
                        .setContentText("Tap to analyze privacy risk")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        manager.notify(packageName.hashCode(), builder.build());
    }
}
