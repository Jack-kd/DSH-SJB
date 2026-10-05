package com.dshmobile.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: classes.dex */
public class SetupActivity extends Activity implements BootstrapInstaller.Listener {
    private Button actionBtn;
    private boolean done;
    private Handler handler;
    private BootstrapInstaller installer;
    private TextView logView;
    private ProgressBar progress;
    private TextView stageText;
    private Thread worker;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.handler = new Handler(Looper.getMainLooper());
        buildUi();
        startInstall();
    }

    private void buildUi() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(Ui.bgSoft(this));
        int iDp = Ui.dp(this, 20.0f);
        linearLayout.setPadding(iDp, iDp, iDp, iDp);
        LinearLayout linearLayoutCard = Ui.card(this);
        linearLayoutCard.addView(Ui.title(this, "初始化 Ubuntu 容器"));
        TextView textViewHint = Ui.hint(this, "首次启动需要联网下载约 400MB（Ubuntu + 编译工具链 + Node.js + DeepSeek Harness），请保持网络畅通。");
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.topMargin = Ui.dp(this, 8.0f);
        linearLayoutCard.addView(textViewHint, layoutParamsMatchWrap);
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        this.progress = progressBar;
        progressBar.setMax(100);
        this.progress.setProgressTintList(ColorStateList.valueOf(Ui.PRIMARY));
        this.progress.setProgressBackgroundTintList(ColorStateList.valueOf(Ui.border(this)));
        LinearLayout.LayoutParams layoutParamsMatchWrap2 = Ui.matchWrap();
        layoutParamsMatchWrap2.topMargin = Ui.dp(this, 16.0f);
        linearLayoutCard.addView(this.progress, layoutParamsMatchWrap2);
        TextView textView = new TextView(this);
        this.stageText = textView;
        textView.setTextColor(Ui.PRIMARY);
        this.stageText.setTextSize(14.0f);
        this.stageText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams layoutParamsMatchWrap3 = Ui.matchWrap();
        layoutParamsMatchWrap3.topMargin = Ui.dp(this, 8.0f);
        linearLayoutCard.addView(this.stageText, layoutParamsMatchWrap3);
        linearLayout.addView(linearLayoutCard, Ui.matchWrap());
        final ScrollView scrollView = new ScrollView(this);
        scrollView.setBackground(Ui.softBg(this));
        int iDp2 = Ui.dp(this, 12.0f);
        scrollView.setPadding(iDp2, iDp2, iDp2, iDp2);
        TextView textView2 = new TextView(this);
        this.logView = textView2;
        textView2.setTextColor(Ui.textSecondary(this));
        this.logView.setTextSize(11.0f);
        this.logView.setTypeface(Typeface.MONOSPACE);
        this.logView.setMovementMethod(new ScrollingMovementMethod());
        scrollView.addView(this.logView);
        LinearLayout.LayoutParams layoutParamsMatchWrap4 = Ui.matchWrap();
        layoutParamsMatchWrap4.topMargin = Ui.dp(this, 16.0f);
        layoutParamsMatchWrap4.weight = 1.0f;
        layoutParamsMatchWrap4.height = 0;
        linearLayout.addView(scrollView, layoutParamsMatchWrap4);
        this.logView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                ScrollView scrollView2 = scrollView;
                scrollView2.post(new Runnable() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda5
                    @Override // java.lang.Runnable
                    public final void run() {
                        scrollView2.fullScroll(130);
                    }
                });
            }
        });
        Button buttonPrimaryButton = Ui.primaryButton(this, "取消");
        this.actionBtn = buttonPrimaryButton;
        buttonPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                SetupActivity.this.lambda$buildUi$2(view);
            }
        });
        LinearLayout.LayoutParams layoutParamsMatchWrap5 = Ui.matchWrap();
        layoutParamsMatchWrap5.topMargin = Ui.dp(this, 16.0f);
        linearLayout.addView(this.actionBtn, layoutParamsMatchWrap5);
        setContentView(linearLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$2(View view) {
        if (this.done) {
            startActivity(new Intent(this, (Class<?>) MainActivity.class));
            finish();
            return;
        }
        BootstrapInstaller bootstrapInstaller = this.installer;
        if (bootstrapInstaller != null) {
            bootstrapInstaller.cancel();
        }
        Thread thread = this.worker;
        if (thread != null) {
            thread.interrupt();
        }
        finish();
    }

    private void startInstall() {
        this.installer = new BootstrapInstaller(this, this);
        final BootstrapInstaller bootstrapInstaller = this.installer;
        Objects.requireNonNull(bootstrapInstaller);
        Thread thread = new Thread(new Runnable() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                    try {
                        bootstrapInstaller.run();
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
            }
        }, "dsh-bootstrap");
        this.worker = thread;
        thread.start();
    }

    @Override // com.dshmobile.app.BootstrapInstaller.Listener
    public void onStage(final String str, final int i) {
        this.handler.post(new Runnable() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {

                SetupActivity.this.lambda$onStage$3(i, str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onStage$3(int i, String str) {
        this.progress.setProgress(i, true);
        TextView textView = this.stageText;
        if (str != null) {
            textView.setText(str + "  " + i + "%");
            return;
        }
        String string = textView.getText().toString();
        int iLastIndexOf = string.lastIndexOf(32);
        TextView textView2 = this.stageText;
        StringBuilder sb = new StringBuilder();
        if (iLastIndexOf > 0) {
            string = string.substring(0, iLastIndexOf);
        }
        textView2.setText(sb.append(string).append(StringUtils.SPACE).append(i).append("%").toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onLog$4(String str) {
        this.logView.append(str + StringUtils.LF);
    }

    @Override // com.dshmobile.app.BootstrapInstaller.Listener
    public void onLog(final String str) {
        this.handler.post(new Runnable() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {

                SetupActivity.this.lambda$onLog$4(str);
            }
        });
    }

    @Override // com.dshmobile.app.BootstrapInstaller.Listener
    public void onDone(final boolean z, final String str) {
        this.handler.post(new Runnable() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {

                SetupActivity.this.lambda$onDone$7(z, str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onDone$7(boolean z, String str) {
        this.done = true;
        TextView textView = this.stageText;
        if (z) {
            textView.setText("安装完成 ✓");
            this.stageText.setTextColor(Ui.PRIMARY);
            this.actionBtn.setText("开始使用");
        } else {
            textView.setText("安装失败：" + str);
            this.stageText.setTextColor(-1751739);
            this.actionBtn.setText("重试");
            this.actionBtn.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {

                    SetupActivity.this.lambda$onDone$6(view);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onDone$6(View view) {
        this.logView.setText("");
        this.progress.setProgress(0);
        this.stageText.setTextColor(Ui.PRIMARY);
        this.actionBtn.setText("取消");
        this.actionBtn.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.SetupActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {

                SetupActivity.this.lambda$onDone$5(view2);
            }
        });
        startInstall();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onDone$5(View view) {
        finish();
    }
}
