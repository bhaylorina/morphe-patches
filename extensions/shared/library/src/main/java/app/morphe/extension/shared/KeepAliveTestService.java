package app.morphe.extension.shared;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.IBinder;
import android.widget.Toast;

public class KeepAliveTestService extends Service {
    private static boolean isTriggered = false;

    public static void init(Application app) {
        if (isTriggered) return;
        isTriggered = true;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Context ctx = app.getApplicationContext();
                Intent i = new Intent(ctx, KeepAliveTestService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    ctx.startForegroundService(i);
                } else {
                    ctx.startService(i);
                }
            } catch (Exception e) {
                isTriggered = false;
                Toast.makeText(app, "FGS Blocked: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, 1500);
    }

    @Override public IBinder onBind(Intent i) { return null; }

    @android.annotation.SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent i, int f, int s) {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                NotificationChannel ch = new NotificationChannel(
                        "fgs_immortal_test",
                        "Keep Alive (Test)",
                        NotificationManager.IMPORTANCE_LOW
                );
                if (nm != null) nm.createNotificationChannel(ch);

                Notification.Builder b = new Notification.Builder(this, "fgs_immortal_test")
                        .setContentTitle("App is Immortal")
                        .setContentText("Background Sync Active")
                        .setSmallIcon(android.R.drawable.ic_menu_info_details);

                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(1002, b.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING);
                } else {
                    startForeground(1002, b.build());
                }
            }
        } catch (Exception e) {}
        return START_STICKY;
    }
}
