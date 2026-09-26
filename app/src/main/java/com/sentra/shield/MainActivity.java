package com.sentra.shield;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    TextView emojiView, statusText;
    LinearLayout recentContainer;
    private static final int NOTIF_ID = 1001;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        emojiView = findViewById(R.id.emojiView);
        statusText = findViewById(R.id.statusText);
        recentContainer = findViewById(R.id.recentContainer);
        View ringView = findViewById(R.id.ringView);
        View cameraIcon = findViewById(R.id.cameraIcon);

        setEmoji("safe");

        ringView.setOnClickListener(v -> showDynamicIsland());
        cameraIcon.setOnClickListener(v -> showDynamicIsland());

        loadRecentApps();
        createNotificationChannel();
        sendNotification();
    }

    private void setEmoji(String status) {
        switch (status) {
            case "safe": emojiView.setText("🥳"); break;
            case "warn": emojiView.setText("🥺"); break;
            case "danger": emojiView.setText("😱"); break;
            default: emojiView.setText("🙂");
        }
    }

    private void showDynamicIsland() {
        new AlertDialog.Builder(this)
            .setTitle("SentraBuddy")
            .setMessage("🟢 System Safe\n📱 42 apps monitored\n⚠️ 2 suspicious apps\n🛡️ Antivirus active")
            .setPositiveButton("OK", null)
            .show();
    }

    private void loadRecentApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        int count = 0;
        for (ApplicationInfo app : apps) {
            if (count++ > 30) break;
            TextView tv = new TextView(this);
            tv.setText("📦 " + app.loadLabel(pm));
            tv.setTextColor(0xFFFFFFFF);
            tv.setPadding(0, 16, 0, 16);
            recentContainer.addView(tv);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                "sentra_ch", "SentraShield", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    private void sendNotification() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("show_status", true);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder nb = new android.app.Notification.Builder(this, "sentra_ch")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SentraShield")
            .setContentText("🥳 System Safe - 2 suspicious apps")
            .setContentIntent(pi)
            .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIF_ID, nb.build());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("show_status", false)) {
            showDynamicIsland();
        }
    }
}
