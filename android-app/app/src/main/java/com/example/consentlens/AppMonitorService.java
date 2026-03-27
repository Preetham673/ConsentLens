package com.example.consentlens;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppMonitorService extends Service {

    private static final String SERVICE_CHANNEL = "ConsentLensMonitor";
    private static final String ALERT_CHANNEL = "ConsentLensRisk";
    private static final int SERVICE_ID = 1;
    private static final String PREFS = "ConsentLensPrefs";
    private static final String KEY_PACKAGES = "known_packages";

    private Handler handler;
    private Set<String> knownPackages;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
        startForegroundServiceNotification();

        handler = new Handler();
        loadKnownPackages();

        handler.postDelayed(checkRunnable, 5000);
    }

    /* =============================
       START FOREGROUND PROPERLY
    ============================== */
    private void startForegroundServiceNotification() {

        Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL)
                .setContentTitle("ConsentLens Active")
                .setContentText("Monitoring new app installations")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(SERVICE_ID, notification);
    }

    /* =============================
       CREATE CHANNELS
    ============================== */
    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (manager == null) return;

            NotificationChannel serviceChannel =
                    new NotificationChannel(
                            SERVICE_CHANNEL,
                            "ConsentLens Monitor",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationChannel alertChannel =
                    new NotificationChannel(
                            ALERT_CHANNEL,
                            "Risk Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(alertChannel);
        }
    }

    /* =============================
       PACKAGE TRACKING
    ============================== */
    private void loadKnownPackages() {

        SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        knownPackages =
                new HashSet<>(prefs.getStringSet(KEY_PACKAGES, new HashSet<>()));

        if (knownPackages.isEmpty()) {

            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps =
                    pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo app : apps) {
                knownPackages.add(app.packageName);
            }

            saveKnownPackages();
        }
    }

    private void saveKnownPackages() {

        SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        prefs.edit().putStringSet(KEY_PACKAGES, knownPackages).apply();
    }

    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            checkInstalledApps();
            handler.postDelayed(this, 5000);
        }
    };

    private void checkInstalledApps() {

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {

            if (!knownPackages.contains(app.packageName)) {

                knownPackages.add(app.packageName);
                saveKnownPackages();
                showRiskNotification(app.packageName);
            }
        }
    }

    private void showRiskNotification(String packageName) {

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager == null) return;

        Intent intent = new Intent(this, RiskAlertActivity.class);
        intent.putExtra("packageName", packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        packageName.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        Notification notification =
                new NotificationCompat.Builder(this, ALERT_CHANNEL)
                        .setContentTitle("New App Installed")
                        .setContentText("Tap to analyze privacy risk")
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        manager.notify(packageName.hashCode(), notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
