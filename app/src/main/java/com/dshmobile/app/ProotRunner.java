package com.dshmobile.app;

import android.content.Context;
import android.system.Os;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.zip.UnixStat;

/* JADX INFO: loaded from: classes.dex */
public final class ProotRunner {
    public static File baseDir(Context context) {
        return new File(context.getFilesDir(), "bootstrap");
    }

    public static File rootfsDir(Context context) {
        return new File(baseDir(context), "rootfs");
    }

    public static File prootBin(Context context) {
        return new File(baseDir(context), "proot");
    }

    public static File libDir(Context context) {
        return new File(baseDir(context), "lib");
    }

    public static void applyEnv(Context context, ProcessBuilder processBuilder) {
        processBuilder.environment().put("PROOT_TMP_DIR", tmpDir(context).getAbsolutePath());
        if (libDir(context).isDirectory()) {
            processBuilder.environment().put("LD_LIBRARY_PATH", libDir(context).getAbsolutePath());
        }
        File file = new File(baseDir(context), "loader");
        if (file.isFile()) {
            processBuilder.environment().put("PROOT_LOADER", file.getAbsolutePath());
        }
    }

    public static File tmpDir(Context context) {
        File file = new File(baseDir(context), "tmp");
        file.mkdirs();
        return file;
    }

    public static File sharedDir() {
        File file = new File("/sdcard/dsh-shared");
        file.mkdirs();
        return file;
    }

    public static File homeDir(Context context) {
        File file = new File(rootfsDir(context), "home/dsh");
        file.mkdirs();
        return file;
    }

    public static List<String> buildCommand(Context context, List<String> list) {
        Prefs prefsOf = Prefs.of(context);
        ArrayList arrayList = new ArrayList();
        arrayList.add(prootBin(context).getAbsolutePath());
        arrayList.add("--kill-on-exit");
        arrayList.add("-0");
        arrayList.add("--link2symlink");
        arrayList.add("-r");
        arrayList.add(rootfsDir(context).getAbsolutePath());
        arrayList.add("-b");
        arrayList.add("/dev");
        arrayList.add("-b");
        arrayList.add("/proc");
        arrayList.add("-b");
        arrayList.add("/sys");
        arrayList.add("-b");
        arrayList.add(tmpDir(context).getAbsolutePath() + ":/tmp");
        File fileHomeDir = homeDir(context);
        String sdPath = prefsOf.getSdPath();
        if (sdPath != null && new File(sdPath).isDirectory()) {
            new File(rootfsDir(context), "mnt/sd").mkdirs();
            arrayList.add("-b");
            arrayList.add(sdPath + ":/mnt/sd");
            new File(fileHomeDir, "sd").mkdirs();
            arrayList.add("-b");
            arrayList.add(sdPath + ":/home/dsh/sd");
        }
        File fileSharedDir = sharedDir();
        if (fileSharedDir.isDirectory()) {
            new File(rootfsDir(context), "mnt/shared").mkdirs();
            arrayList.add("-b");
            arrayList.add(fileSharedDir.getAbsolutePath() + ":/mnt/shared");
            new File(fileHomeDir, "shared").mkdirs();
            arrayList.add("-b");
            arrayList.add(fileSharedDir.getAbsolutePath() + ":/home/dsh/shared");
        }
        arrayList.add("-w");
        arrayList.add("/home/dsh");
        arrayList.add("/usr/bin/env");
        arrayList.add("-u");
        arrayList.add("LD_LIBRARY_PATH");
        arrayList.add("PATH=/opt/node/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
        arrayList.add("HOME=/home/dsh");
        arrayList.add("TERM=xterm-256color");
        arrayList.add("DEBIAN_FRONTEND=noninteractive");
        arrayList.add("DSH_PERMISSION_MODE=danger-full-access");
        arrayList.addAll(list);
        return arrayList;
    }

    public static Process exec(Context context, List<String> list, File file) throws IOException {
        ensureExecutable(context);
        ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(context, list));
        applyEnv(context, processBuilder);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
        return processBuilder.start();
    }

    public static Process execPiped(Context context, List<String> list) throws IOException {
        ensureExecutable(context);
        ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(context, list));
        applyEnv(context, processBuilder);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    private static void ensureExecutable(Context context) {
        File fileProotBin = prootBin(context);
        if (fileProotBin.isFile()) {
            try {
                Os.chmod(fileProotBin.getAbsolutePath(), UnixStat.DEFAULT_DIR_PERM);
            } catch (Exception unused) {
                fileProotBin.setExecutable(true, true);
            }
        }
    }

    public static Process startWeb(Context context, int i, File file) throws IOException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("dsh");
        arrayList.add("web");
        arrayList.add("--host");
        arrayList.add("127.0.0.1");
        arrayList.add("--port");
        arrayList.add(String.valueOf(i));
        return exec(context, arrayList, file);
    }
}
