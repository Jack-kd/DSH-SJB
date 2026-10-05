package com.dshmobile.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class SettingsActivity extends Activity {
    private LinearLayout list;
    private Prefs prefs;

    /* JADX INFO: Access modifiers changed from: private */
    interface OnText {
        void accept(String str);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.prefs = Prefs.of(this);
        buildUi();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.list != null) {
            refresh();
        }
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Ui.bgSoft(this));
        LinearLayout linearLayout = new LinearLayout(this);
        this.list = linearLayout;
        linearLayout.setOrientation(1);
        int iDp = Ui.dp(this, 20.0f);
        this.list.setPadding(iDp, iDp, iDp, iDp);
        scrollView.addView(this.list);
        setContentView(scrollView);
        fillRows();
    }

    private void fillRows() {
        TextView textViewTitle = Ui.title(this, "设置");
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.bottomMargin = Ui.dp(this, 4.0f);
        this.list.addView(textViewTitle, layoutParamsMatchWrap);
        addHeader("SD 卡映射（容器 /mnt/sd）");
        LinearLayout linearLayoutCard = Ui.card(this);
        addRow(linearLayoutCard, "所有文件访问权限", Environment.isExternalStorageManager() ? "已授权 ✓" : "未授权 — 点这里去开启", new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda20
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$0(view);
            }
        });
        linearLayoutCard.addView(Ui.divider(this));
        addRow(linearLayoutCard, "外置 SD 目录", sdDisplay(), new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda26
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$1(view);
            }
        });
        linearLayoutCard.addView(Ui.divider(this));
        addRow(linearLayoutCard, "共享存储兜底", "/sdcard/dsh-shared → /mnt/shared（自动创建）", null);
        this.list.addView(linearLayoutCard, cardLp());
        addHeader("服务");
        LinearLayout linearLayoutCard2 = Ui.card(this);
        addRow(linearLayoutCard2, "Web 端口", String.valueOf(this.prefs.getPort()), new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$3(view);
            }
        });
        linearLayoutCard2.addView(Ui.divider(this));
        addRow(linearLayoutCard2, "服务状态", HarnessService.isRunning() ? "运行中" : "已停止", null);
        this.list.addView(linearLayoutCard2, cardLp());
        LinearLayout linearLayout = new LinearLayout(this);
        boolean z = false;
        linearLayout.setOrientation(0);
        Button buttonPrimaryButton = Ui.primaryButton(this, HarnessService.isRunning() ? "停止服务" : "启动服务");
        buttonPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$4(view);
            }
        });
        linearLayout.addView(buttonPrimaryButton, new LinearLayout.LayoutParams(0, Ui.dp(this, 48.0f), 1.0f));
        Button buttonOutlineButton = Ui.outlineButton(this, "重启服务");
        buttonOutlineButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$6(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 48.0f), 1.0f);
        layoutParams.leftMargin = Ui.dp(this, 12.0f);
        linearLayout.addView(buttonOutlineButton, layoutParams);
        LinearLayout.LayoutParams layoutParamsMatchWrap2 = Ui.matchWrap();
        layoutParamsMatchWrap2.topMargin = Ui.dp(this, 12.0f);
        this.list.addView(linearLayout, layoutParamsMatchWrap2);
        addHeader("后台保活（防止划卡后服务被杀）");
        LinearLayout linearLayoutCard3 = Ui.card(this);
        PowerManager powerManager = (PowerManager) getSystemService("power");
        if (powerManager != null && powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            z = true;
        }
        addRow(linearLayoutCard3, "忽略电池优化", z ? "已加入白名单 ✓" : "未加入 — 点这里去开启", new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$7(view);
            }
        });
        linearLayoutCard3.addView(Ui.divider(this));
        addRow(linearLayoutCard3, "自启动 / 允许后台活动", "荣耀等机型必须手动开启，点这里跳到应用详情：耗电详情/启动管理 → 允许自启动、允许后台活动", new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$8(view);
            }
        });
        this.list.addView(linearLayoutCard3, cardLp());
        addHeader("下载镜像（下次安装生效）");
        LinearLayout linearLayoutCard4 = Ui.card(this);
        addRow(linearLayoutCard4, "rootfs 地址", this.prefs.getRootfsUrl(), new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$10(view);
            }
        });
        linearLayoutCard4.addView(Ui.divider(this));
        addRow(linearLayoutCard4, "Node.js 镜像", this.prefs.getNodeMirror(), new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$12(view);
            }
        });
        linearLayoutCard4.addView(Ui.divider(this));
        addRow(linearLayoutCard4, "npm registry", this.prefs.getNpmRegistry(), new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$14(view);
            }
        });
        this.list.addView(linearLayoutCard4, cardLp());
        addHeader("维护");
        Button buttonOutlineButton2 = Ui.outlineButton(this, "文件管理器（传入/导出）");
        buttonOutlineButton2.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda21
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$15(view);
            }
        });
        this.list.addView(buttonOutlineButton2, btnLp());
        Button buttonOutlineButton3 = Ui.outlineButton(this, "打开容器终端");
        buttonOutlineButton3.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda22
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$16(view);
            }
        });
        this.list.addView(buttonOutlineButton3, btnLp());
        Button buttonOutlineButton4 = Ui.outlineButton(this, "查看运行日志");
        buttonOutlineButton4.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda23
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$17(view);
            }
        });
        this.list.addView(buttonOutlineButton4, btnLp());
        Button buttonOutlineButton5 = Ui.outlineButton(this, "查看安装日志");
        buttonOutlineButton5.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda24
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$18(view);
            }
        });
        this.list.addView(buttonOutlineButton5, btnLp());
        Button buttonOutlineButton6 = Ui.outlineButton(this, "重置容器（删除全部数据）");
        buttonOutlineButton6.setTextColor(-1751739);
        buttonOutlineButton6.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda25
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SettingsActivity.this.lambda$fillRows$22(view);
            }
        });
        this.list.addView(buttonOutlineButton6, btnLp());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$0(View view) {
        if (Environment.isExternalStorageManager()) {
            return;
        }
        try {
            Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception unused) {
            startActivity(new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$1(View view) {
        pickSdDir();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$3(View view) {
        editText("Web 端口", String.valueOf(this.prefs.getPort()), new OnText() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda19
            @Override // com.dshmobile.app.SettingsActivity.OnText
            public final void accept(String str) {

                SettingsActivity.this.lambda$fillRows$2(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$2(String str) {
        try {
            int i = Integer.parseInt(str.trim());
            if (i <= 0 || i >= 65536) {
                return;
            }
            this.prefs.setPort(i);
            toast("端口已保存，重启服务后生效");
            refresh();
        } catch (NumberFormatException unused) {
            toast("端口无效");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$4(View view) {
        if (HarnessService.isRunning()) {
            HarnessService.stopService(this);
        } else {
            HarnessService.startService(this);
        }
        this.list.postDelayed(new Runnable() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {

                SettingsActivity.this.refresh();
            }
        }, 800L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$6(View view) {
        HarnessService.stopService(this);
        this.list.postDelayed(new Runnable() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {

                SettingsActivity.this.lambda$fillRows$5();
            }
        }, 1500L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$5() {
        HarnessService.startService(this);
        refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$7(View view) {
        PowerManager powerManager = (PowerManager) getSystemService("power");
        if (powerManager == null || powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            return;
        }
        try {
            Intent intent = new Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception unused) {
            toast("无法打开电池优化设置");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$8(View view) {
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception unused) {
            toast("无法打开应用详情页");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$10(View view) {
        editText("rootfs 地址", this.prefs.getRootfsUrl(), new OnText() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda0
            @Override // com.dshmobile.app.SettingsActivity.OnText
            public final void accept(String str) {

                SettingsActivity.this.lambda$fillRows$9(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$9(String str) {
        this.prefs.setRootfsUrl(str.trim());
        refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$12(View view) {
        editText("Node.js 镜像", this.prefs.getNodeMirror(), new OnText() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda17
            @Override // com.dshmobile.app.SettingsActivity.OnText
            public final void accept(String str) {

                SettingsActivity.this.lambda$fillRows$11(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$11(String str) {
        this.prefs.setNodeMirror(str.trim());
        refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$14(View view) {
        editText("npm registry", this.prefs.getNpmRegistry(), new OnText() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda11
            @Override // com.dshmobile.app.SettingsActivity.OnText
            public final void accept(String str) {

                SettingsActivity.this.lambda$fillRows$13(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$13(String str) {
        this.prefs.setNpmRegistry(str.trim());
        refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$15(View view) {
        startActivity(new Intent(this, (Class<?>) FileManagerActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$16(View view) {
        startActivity(new Intent(this, (Class<?>) TerminalActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$17(View view) {
        showLog("dsh-web.log");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$18(View view) {
        showLog("install.log");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$22(View view) {
        new AlertDialog.Builder(this).setTitle("重置容器").setMessage("将删除 Ubuntu 容器及其中全部数据，下次启动重新安装。确定？").setPositiveButton("重置", new DialogInterface.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda12
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {

                SettingsActivity.this.lambda$fillRows$21(dialogInterface, i);
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$21(DialogInterface dialogInterface, int i) {
        HarnessService.stopService(this);
        new Thread(new Runnable() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {

                SettingsActivity.this.lambda$fillRows$20();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$20() {
        BootstrapInstaller.deleteRecursively(ProotRunner.baseDir(this));
        this.prefs.setSetupDone(false);
        runOnUiThread(new Runnable() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {

                SettingsActivity.this.lambda$fillRows$19();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fillRows$19() {
        startActivity(new Intent(this, (Class<?>) SetupActivity.class));
        finish();
    }

    private String sdDisplay() {
        String sdPath = this.prefs.getSdPath();
        if (sdPath == null) {
            return "未设置（仅使用 /mnt/shared 兜底）";
        }
        return sdPath + (new File(sdPath).isDirectory() ? "" : "  （路径不存在！）");
    }

    private void pickSdDir() {
        if (!Environment.isExternalStorageManager()) {
            toast("请先开启「所有文件访问权限」");
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("不映射（仅 /mnt/shared）");
        File[] fileArrListFiles = new File("/storage").listFiles();
        if (fileArrListFiles != null) {
            for (File file : fileArrListFiles) {
                if (file.isDirectory() && !file.getName().equals("emulated") && !file.getName().equals("self")) {
                    arrayList.add(file.getAbsolutePath());
                }
            }
        }
        arrayList.add("手动输入路径…");
        final String[] strArr = (String[]) arrayList.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("选择外置 SD 目录").setItems(strArr, new DialogInterface.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda14
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {

                SettingsActivity.this.lambda$pickSdDir$24(strArr, dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$pickSdDir$24(String[] strArr, DialogInterface dialogInterface, int i) {
        String str = strArr[i];
        if (i == 0) {
            this.prefs.setSdPath(null);
            refresh();
        } else {
            if (str.startsWith("/storage")) {
                String str2 = str + "/dsh";
                new File(str2).mkdirs();
                this.prefs.setSdPath(str2);
                toast("已映射 " + str2 + " → /mnt/sd，重启服务生效");
                refresh();
                return;
            }
            editText("SD 目录完整路径", this.prefs.getSdPath() == null ? "" : this.prefs.getSdPath(), new OnText() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda18
                @Override // com.dshmobile.app.SettingsActivity.OnText
                public final void accept(String str3) {

                    SettingsActivity.this.lambda$pickSdDir$23(str3);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$pickSdDir$23(String str) {
        this.prefs.setSdPath(str.trim());
        refresh();
    }

    private void showLog(String str) {
        File file = new File(ProotRunner.baseDir(this), str);
        StringBuilder sb = new StringBuilder();
        if (file.isFile()) {
            try {
                byte[] allBytes = Files.readAllBytes(file.toPath());
                int iMax = Math.max(0, allBytes.length - 60000);
                sb.append(new String(allBytes, iMax, allBytes.length - iMax));
            } catch (Exception e) {
                sb.append("读取失败: ").append(e.getMessage());
            }
        } else {
            sb.append("（暂无日志）");
        }
        TextView textView = new TextView(this);
        textView.setText(sb.toString());
        textView.setTextSize(11.0f);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setTextIsSelectable(true);
        int iDp = Ui.dp(this, 12.0f);
        textView.setPadding(iDp, iDp, iDp, iDp);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(textView);
        new AlertDialog.Builder(this).setTitle(str).setView(scrollView).setPositiveButton("关闭", (DialogInterface.OnClickListener) null).show();
    }

    private void editText(String str, String str2, final OnText onText) {
        final EditText editText = new EditText(this);
        editText.setText(str2);
        editText.setSingleLine(true);
        int iDp = Ui.dp(this, 16.0f);
        int i = iDp / 2;
        editText.setPadding(iDp, i, iDp, i);
        new AlertDialog.Builder(this).setTitle(str).setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.dshmobile.app.SettingsActivity$$ExternalSyntheticLambda9
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i2) {
                onText.accept(editText.getText().toString());
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    private void addHeader(String str) {
        TextView textViewSectionHeader = Ui.sectionHeader(this, str);
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.topMargin = Ui.dp(this, 20.0f);
        layoutParamsMatchWrap.bottomMargin = Ui.dp(this, 8.0f);
        this.list.addView(textViewSectionHeader, layoutParamsMatchWrap);
    }

    private void addRow(LinearLayout linearLayout, String str, String str2, View.OnClickListener onClickListener) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(0, Ui.dp(this, 10.0f), 0, Ui.dp(this, 10.0f));
        linearLayout2.addView(Ui.body(this, str));
        if (str2 != null) {
            TextView textViewHint = Ui.hint(this, str2);
            textViewHint.setTextSize(12.0f);
            linearLayout2.addView(textViewHint);
        }
        if (onClickListener != null) {
            linearLayout2.setOnClickListener(onClickListener);
        }
        linearLayout.addView(linearLayout2, Ui.matchWrap());
    }

    private LinearLayout.LayoutParams cardLp() {
        return Ui.matchWrap();
    }

    private LinearLayout.LayoutParams btnLp() {
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.topMargin = Ui.dp(this, 10.0f);
        return layoutParamsMatchWrap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refresh() {
        this.list.removeAllViews();
        fillRows();
    }

    private void toast(String str) {
        Toast.makeText(this, str, 0).show();
    }
}
