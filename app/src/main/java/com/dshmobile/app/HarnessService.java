package com.dshmobile.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.time.DateUtils;

/* JADX INFO: loaded from: classes.dex */
public class HarnessService extends Service {
    public static final String ACTION_START = "com.dshmobile.app.action.START";
    public static final String ACTION_STOP = "com.dshmobile.app.action.STOP";
    private static final String CHANNEL_ID = "harness";
    private static final int MAX_RESTART = 5;
    private static final int NOTIF_ID = 1001;
    private static Process process;
    private static boolean running;
    private ExecutorService executor;
    private PowerManager.WakeLock wakeLock;
    private volatile boolean wantRun;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning() {
        Process process2;
        return running && (process2 = process) != null && process2.isAlive();
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, (Class<?>) HarnessService.class);
        intent.setAction(ACTION_START);
        context.startForegroundService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, (Class<?>) HarnessService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.executor = Executors.newSingleThreadExecutor();
        createChannel();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (ACTION_STOP.equals(intent == null ? ACTION_START : intent.getAction())) {
            this.wantRun = false;
            stopContainer();
            stopForeground(true);
            stopSelf();
            return 2;
        }
        startForeground(1001, buildNotification("正在启动容器…"));
        acquireWakeLock();
        this.wantRun = true;
        if (!isRunning()) {
            this.executor.execute(new Runnable() { // from class: com.dshmobile.app.HarnessService$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {

                    HarnessService.this.runLoop();
                }
            });
        }
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runLoop() {
        String str;
        Prefs prefsOf = Prefs.of(this);
        int i = 0;
        while (this.wantRun) {
            File file = new File(ProotRunner.baseDir(this), "dsh-web.log");
            if (NodePtyFixer.needsFix(ProotRunner.rootfsDir(this))) {
                updateNotification("正在修复 node-pty 原生模块…");
                if (NodePtyFixer.fix(this, file)) {
                    str = "node-pty 修复完成，正在启动…";
                } else {
                    str = "node-pty 修复失败，请到设置查看日志";
                }
                updateNotification(str);
            }
            try {
                updateNotification("DeepSeek Harness 运行中 · 端口 " + prefsOf.getPort());
                Process processStartWeb = ProotRunner.startWeb(this, prefsOf.getPort(), file);
                process = processStartWeb;
                running = true;
                int iWaitFor = processStartWeb.waitFor();
                running = false;
                if (!this.wantRun) {
                    break;
                }
                i++;
                if (i > 5) {
                    updateNotification("容器多次退出，已停止（详见日志）");
                    break;
                } else {
                    updateNotification("容器退出(" + iWaitFor + ")，3 秒后重启…");
                    Thread.sleep(3000L);
                }
            } catch (IOException e) {
                running = false;
                updateNotification("启动失败: " + e.getMessage());
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        }
        running = false;
        releaseWakeLock();
        if (this.wantRun) {
            return;
        }
        stopForeground(true);
        stopSelf();
    }

    private void stopContainer() {
        Process process2 = process;
        if (process2 != null) {
            process2.destroy();
            try {
                process2.waitFor();
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
            process2.destroyForcibly();
        }
        running = false;
    }

    private void acquireWakeLock() {
        if (this.wakeLock == null) {
            PowerManager.WakeLock wakeLockNewWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, "dshmobile:harness");
            this.wakeLock = wakeLockNewWakeLock;
            wakeLockNewWakeLock.setReferenceCounted(false);
        }
        if (this.wakeLock.isHeld()) {
            return;
        }
        this.wakeLock.acquire(DateUtils.MILLIS_PER_DAY);
    }

    private void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.wakeLock;
        if (wakeLock == null || !wakeLock.isHeld()) {
            return;
        }
        this.wakeLock.release();
    }

    private void createChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Harness 服务", 2);
        notificationChannel.setDescription("DeepSeek Harness 容器运行状态");
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private Notification buildNotification(String str) {
        Notification.Builder builder;
        PendingIntent activity = PendingIntent.getActivity(this, 0, new Intent(this, (Class<?>) MainActivity.class), 201326592);
        Intent intent = new Intent(this, (Class<?>) HarnessService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent service = PendingIntent.getService(this, 1, intent, 201326592);
        if (Build.VERSION.SDK_INT >= 31) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this, CHANNEL_ID);
        }
        return builder.setContentTitle(getString(R.string.app_name)).setContentText(str).setSmallIcon(android.R.drawable.stat_sys_download_done).setContentIntent(activity).addAction(new Notification.Action.Builder((Icon) null, "停止", service).build()).setOngoing(true).build();
    }

    private void updateNotification(String str) {
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(1001, buildNotification(str));
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.wantRun = false;
        stopContainer();
        releaseWakeLock();
        ExecutorService executorService = this.executor;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent intent) {
        if (this.wantRun) {
            try {
                Intent intent2 = new Intent(this, (Class<?>) HarnessService.class);
                intent2.setAction(ACTION_START);
                startForegroundService(intent2);
            } catch (Exception unused) {
            }
        }
        super.onTaskRemoved(intent);
    }
}
