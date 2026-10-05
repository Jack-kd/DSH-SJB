package com.dshmobile.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class MainActivity extends Activity {
    private Handler handler;
    private MobileUiInjector injector;
    private int loadAttempts;
    private volatile boolean pageFailed;
    private int port;
    private FrameLayout splash;
    private TextView splashStatus;
    private volatile boolean stopPolling;
    private WebView webView;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        Prefs prefsOf = Prefs.of(this);
        if (!prefsOf.isSetupDone()) {
            startActivity(new Intent(this, (Class<?>) SetupActivity.class));
            finish();
            return;
        }
        this.port = prefsOf.getPort();
        this.handler = new Handler(Looper.getMainLooper());
        this.injector = new MobileUiInjector(this);
        ArrayList arrayList = new ArrayList();
        if (Build.VERSION.SDK_INT < 33) {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                arrayList.add("android.permission.WRITE_EXTERNAL_STORAGE");
            }
        } else if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            arrayList.add("android.permission.POST_NOTIFICATIONS");
        }
        if (!arrayList.isEmpty()) {
            requestPermissions((String[]) arrayList.toArray(new String[0]), 1);
        }
        buildUi();
        HarnessService.startService(this);
        waitForServerAndLoad();
    }

    private void buildUi() {
        FrameLayout frameLayout = new FrameLayout(this);
        WebView webView = new WebView(this);
        this.webView = webView;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.setWebViewClient(new WebViewClient() { // from class: com.dshmobile.app.MainActivity.1
            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView2, String str, Bitmap bitmap) {
                MainActivity.this.pageFailed = false;
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView2, String str) {
                if (MainActivity.this.pageFailed) {
                    return;
                }
                MainActivity.this.injector.inject(webView2);
                MainActivity.this.dismissSplash();
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView2, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                if (webResourceRequest.isForMainFrame()) {
                    MainActivity.this.pageFailed = true;
                    MainActivity.this.scheduleReload();
                }
            }
        });
        this.webView.setWebChromeClient(new WebChromeClient());
        this.webView.addJavascriptInterface(new DshBridge(), "DshNative");
        frameLayout.addView(this.webView, new FrameLayout.LayoutParams(-1, -1));
        frameLayout.addView(buildFabs());
        FrameLayout frameLayoutBuildSplash = buildSplash();
        this.splash = frameLayoutBuildSplash;
        frameLayout.addView(frameLayoutBuildSplash, new FrameLayout.LayoutParams(-1, -1));
        setContentView(frameLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class DshBridge {
        private DshBridge() {
        }

        @JavascriptInterface
        public void openConfig() {
            MainActivity.this.handler.post(new Runnable() { // from class: com.dshmobile.app.MainActivity$DshBridge$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {

                    DshBridge.this.lambda$openConfig$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$openConfig$0() {
            if (MainActivity.this.isFinishing()) {
                return;
            }
            MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) ConfigEditorActivity.class));
        }

        @JavascriptInterface
        public void installPlugin(final String str) {
            new Thread(new Runnable() { // from class: com.dshmobile.app.MainActivity$DshBridge$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {

                    DshBridge.this.lambda$installPlugin$2(str);
                }
            }, "dsh-plugin-install").start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$installPlugin$2(String str) {
            PluginInstaller.Result resultInstall = PluginInstaller.install(MainActivity.this, str);
            final String str2 = "{\"ok\":" + resultInstall.ok + ",\"output\":" + JSONObject.quote(resultInstall.output) + "}";
            MainActivity.this.handler.post(new Runnable() { // from class: com.dshmobile.app.MainActivity$DshBridge$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {

                    DshBridge.this.lambda$installPlugin$1(str2);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$installPlugin$1(String str) {
            if (MainActivity.this.webView != null) {
                MainActivity.this.webView.evaluateJavascript("window.__dshOnPluginInstallResult && window.__dshOnPluginInstallResult(" + str + ")", null);
            }
        }
    }

    private View buildFabs() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.addView(fab("📁", new View.OnClickListener() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                MainActivity.this.lambda$buildFabs$0(view);
            }
        }));
        View viewFab = fab(">_", new View.OnClickListener() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                MainActivity.this.lambda$buildFabs$1(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewFab.getLayoutParams();
        layoutParams.topMargin = Ui.dp(this, 12.0f);
        linearLayout.addView(viewFab, layoutParams);
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 8388629;
        layoutParams2.rightMargin = Ui.dp(this, 6.0f);
        linearLayout.setLayoutParams(layoutParams2);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildFabs$0(View view) {
        startActivity(new Intent(this, (Class<?>) FileManagerActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildFabs$1(View view) {
        startActivity(new Intent(this, (Class<?>) TerminalActivity.class));
    }

    private View fab(String str, View.OnClickListener onClickListener) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(15.0f);
        textView.setTypeface(Typeface.MONOSPACE, 1);
        textView.setGravity(17);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(1);
        gradientDrawable.setColor(Ui.PRIMARY);
        textView.setBackground(gradientDrawable);
        textView.setAlpha(0.85f);
        int iDp = Ui.dp(this, 44.0f);
        textView.setLayoutParams(new LinearLayout.LayoutParams(iDp, iDp));
        textView.setOnClickListener(onClickListener);
        return textView;
    }

    private FrameLayout buildSplash() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Ui.bg(this));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(17);
        int iDp = Ui.dp(this, 36.0f);
        linearLayout.setPadding(iDp, iDp, iDp, iDp);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_dsh_brand, null));
        imageView.setImageTintList(ColorStateList.valueOf(Ui.text(this)));
        int iDp2 = Ui.dp(this, 96.0f);
        linearLayout.addView(imageView, new LinearLayout.LayoutParams(iDp2, iDp2));
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(Ui.PRIMARY));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = Ui.dp(this, 32.0f);
        linearLayout.addView(progressBar, layoutParams);
        TextView textViewHint = Ui.hint(this, "正在启动容器…");
        this.splashStatus = textViewHint;
        textViewHint.setGravity(17);
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.topMargin = Ui.dp(this, 16.0f);
        linearLayout.addView(this.splashStatus, layoutParamsMatchWrap);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(17);
        linearLayout2.setOrientation(0);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.topMargin = Ui.dp(this, 40.0f);
        Button buttonOutlineButton = Ui.outlineButton(this, "设置");
        buttonOutlineButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                MainActivity.this.lambda$buildSplash$2(view);
            }
        });
        linearLayout2.addView(buttonOutlineButton);
        Button buttonOutlineButton2 = Ui.outlineButton(this, "终端");
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, Ui.dp(this, 48.0f));
        layoutParams3.leftMargin = Ui.dp(this, 16.0f);
        buttonOutlineButton2.setLayoutParams(layoutParams3);
        buttonOutlineButton2.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                MainActivity.this.lambda$buildSplash$3(view);
            }
        });
        linearLayout2.addView(buttonOutlineButton2);
        Button buttonPrimaryButton = Ui.primaryButton(this, "重新安装");
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-2, Ui.dp(this, 48.0f));
        layoutParams4.leftMargin = Ui.dp(this, 16.0f);
        buttonPrimaryButton.setLayoutParams(layoutParams4);
        buttonPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                MainActivity.this.lambda$buildSplash$4(view);
            }
        });
        linearLayout2.addView(buttonPrimaryButton);
        linearLayout.addView(linearLayout2, layoutParams2);
        FrameLayout.LayoutParams layoutParams5 = new FrameLayout.LayoutParams(-2, -2);
        layoutParams5.gravity = 17;
        frameLayout.addView(linearLayout, layoutParams5);
        return frameLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSplash$2(View view) {
        startActivity(new Intent(this, (Class<?>) SettingsActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSplash$3(View view) {
        startActivity(new Intent(this, (Class<?>) TerminalActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSplash$4(View view) {
        startActivity(new Intent(this, (Class<?>) SetupActivity.class));
    }

    private void waitForServerAndLoad() {
        this.stopPolling = false;
        new Thread(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {

                MainActivity.this.lambda$waitForServerAndLoad$7();
            }
        }, "dsh-port-poll").start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$waitForServerAndLoad$7() {
        long jCurrentTimeMillis = System.currentTimeMillis() + 120000;
        while (!this.stopPolling && System.currentTimeMillis() < jCurrentTimeMillis) {
            if (!httpReady(this.port)) {
                final long jCurrentTimeMillis2 = (jCurrentTimeMillis - System.currentTimeMillis()) / 1000;
                this.handler.post(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.lambda$waitForServerAndLoad$5(jCurrentTimeMillis2);
                    }
                });
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException unused) {
                    return;
                }
            } else {
                this.handler.post(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.lambda$waitForServerAndLoad$6(true);
                    }
                });
                return;
            }
        }
        this.handler.post(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$waitForServerAndLoad$6(false);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$waitForServerAndLoad$5(long j) {
        String str;
        TextView textView = this.splashStatus;
        if (HarnessService.isRunning()) {
            str = "容器已启动，等待 Web 服务就绪… (" + j + "s)";
        } else {
            str = "正在启动容器… (" + j + "s)";
        }
        textView.setText(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$waitForServerAndLoad$6(boolean z) {
        if (isFinishing()) {
            return;
        }
        TextView textView = this.splashStatus;
        if (z) {
            textView.setText("正在加载界面…");
            loadMainUrl();
        } else {
            textView.setText("等待超时。请到设置查看日志，或点“重新安装”。");
        }
    }

    private void loadMainUrl() {
        this.loadAttempts++;
        this.webView.loadUrl("http://127.0.0.1:" + this.port + "/");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleReload() {
        this.handler.post(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {

                MainActivity.this.lambda$scheduleReload$9();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$scheduleReload$9() {
        if (isFinishing()) {
            return;
        }
        if (this.loadAttempts >= 15) {
            showSplash("界面加载失败。请到设置查看日志，或点“重新安装”。");
        } else {
            showSplash("连接 Web 服务失败，正在重试…");
            this.handler.postDelayed(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {

                    MainActivity.this.lambda$scheduleReload$8();
                }
            }, 2000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$scheduleReload$8() {
        if (isFinishing()) {
            return;
        }
        loadMainUrl();
    }

    private void showSplash(String str) {
        this.splash.animate().cancel();
        this.splash.setAlpha(1.0f);
        this.splash.setVisibility(0);
        this.splashStatus.setText(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissSplash() {
        if (this.splash.getVisibility() != 0) {
            return;
        }
        this.splash.animate().alpha(0.0f).setDuration(300L).withEndAction(new Runnable() { // from class: com.dshmobile.app.MainActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {

                MainActivity.this.lambda$dismissSplash$10();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$dismissSplash$10() {
        this.splash.setVisibility(8);
        this.splash.setAlpha(1.0f);
    }

    private static boolean httpReady(int i) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://127.0.0.1:" + i + "/").openConnection();
            try {
                httpURLConnection.setConnectTimeout(800);
                httpURLConnection.setReadTimeout(1500);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.getResponseCode();
                return true;
            } finally {
                httpURLConnection.disconnect();
            }
        } catch (IOException unused) {
            return false;
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        WebView webView = this.webView;
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        WebView webView = this.webView;
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.webView == null || this.splash.getVisibility() != 0) {
            return;
        }
        if (this.webView.getUrl() == null || this.pageFailed) {
            this.loadAttempts = 0;
            waitForServerAndLoad();
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        WebView webView = this.webView;
        if (webView != null && webView.canGoBack()) {
            this.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.stopPolling = true;
        WebView webView = this.webView;
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
