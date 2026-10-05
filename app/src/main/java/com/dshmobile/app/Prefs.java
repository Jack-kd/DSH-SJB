package com.dshmobile.app;

import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes.dex */
public final class Prefs {
    public static final String DEFAULT_NODE_MIRROR = "https://mirrors.ustc.edu.cn/node";
    public static final String DEFAULT_NPM_REGISTRY = "https://registry.npmmirror.com";
    public static final int DEFAULT_PORT = 3080;
    public static final String DEFAULT_ROOTFS_URL = "https://mirrors.ustc.edu.cn/ubuntu-cdimage/ubuntu-base/releases/22.04/release/ubuntu-base-22.04.5-base-arm64.tar.gz";
    public static final String KEY_NODE_MIRROR = "node_mirror";
    public static final String KEY_NPM_REGISTRY = "npm_registry";
    public static final String KEY_PORT = "port";
    public static final String KEY_ROOTFS_URL = "rootfs_url";
    public static final String KEY_SD_PATH = "sd_path";
    public static final String KEY_SETUP_DONE = "setup_done";
    private static final String NAME = "dsh_settings";
    private final SharedPreferences sp;

    private Prefs(Context context) {
        this.sp = context.getApplicationContext().getSharedPreferences(NAME, 0);
    }

    public static Prefs of(Context context) {
        return new Prefs(context);
    }

    public String getSdPath() {
        return this.sp.getString(KEY_SD_PATH, null);
    }

    public void setSdPath(String str) {
        this.sp.edit().putString(KEY_SD_PATH, str).apply();
    }

    public int getPort() {
        return this.sp.getInt(KEY_PORT, DEFAULT_PORT);
    }

    public void setPort(int i) {
        this.sp.edit().putInt(KEY_PORT, i).apply();
    }

    public String getRootfsUrl() {
        return this.sp.getString(KEY_ROOTFS_URL, DEFAULT_ROOTFS_URL);
    }

    public void setRootfsUrl(String str) {
        this.sp.edit().putString(KEY_ROOTFS_URL, str).apply();
    }

    public String getNodeMirror() {
        return this.sp.getString(KEY_NODE_MIRROR, DEFAULT_NODE_MIRROR);
    }

    public void setNodeMirror(String str) {
        this.sp.edit().putString(KEY_NODE_MIRROR, str).apply();
    }

    public String getNpmRegistry() {
        return this.sp.getString(KEY_NPM_REGISTRY, DEFAULT_NPM_REGISTRY);
    }

    public void setNpmRegistry(String str) {
        this.sp.edit().putString(KEY_NPM_REGISTRY, str).apply();
    }

    public boolean isSetupDone() {
        return this.sp.getBoolean(KEY_SETUP_DONE, false);
    }

    public void setSetupDone(boolean z) {
        this.sp.edit().putBoolean(KEY_SETUP_DONE, z).apply();
    }
}
