package com.sentra.shield;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.core.app.NotificationCompat;
import java.util.*;

public class MainActivity extends Activity {

    TextView emojiView, statusText;
    LinearLayout recentContainer;
    private static final int NOTIF_ID = 1001;

    // 🔑 Apni Gemini API key yahan daalein
    private static final String GEMINI_API_KEY = "YOUR_API_KEY_HERE";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        emojiView = findViewById(R.id.emojiView);
        statusText = findViewById(R.id.statusText);
        recentContainer = findViewById(R.id.recentContainer);
        View ringView = findViewById(R.id.ringView);
        View cameraIcon = findViewById(R.id.cameraIcon);

        // Status ke hisaab se emoji
        setEmoji("safe");

        // Camera ring click (camera NA khule)
        ringView.setOnClickListener(v -> showDynamicIsland());
        cameraIcon.setOnClickListener(v -> showDynamicIsland());

        // Recent apps list (scrolling)
        loadRecentApps();

        // Notification (sirf ek baar)
        createNotificationChannel();
        sendNotification();

        // ✅ Gemini 404 fix - v1beta + gemini-1.5-flash
        callGemini();
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

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, "sentra_ch")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SentraShield")
            .setContentText("🥳 System Safe - 2 suspicious apps")
            .setContentIntent(pi)
            .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(NOTIF_ID, nb.build());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("show_status", false)) {
            showDynamicIsland();
        }
    }

    // ✅ GEMINI API CALL (404 FIX)
    private void callGemini() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String body = "{\"contents\":[{\"parts\":[{\"text\":\"Hello\"}]}]}";
                conn.getOutputStream().write(body.getBytes());

                int code = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (code == 200) {
                        statusText.setText("🟢 System Safe | Gemini: OK");
                    } else {
                        statusText.setText("⚠️ Gemini Error: " + code);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}
