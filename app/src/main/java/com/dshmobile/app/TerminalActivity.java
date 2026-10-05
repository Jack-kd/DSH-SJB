package com.dshmobile.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: classes.dex */
public class TerminalActivity extends Activity {
    private static final int KEEP_BUFFER = 150000;
    private static final int MAX_BUFFER = 200000;
    private boolean flushPending;
    private Handler handler;
    private EditText input;
    private TextView output;
    private ScrollView scroll;
    private Process shell;
    private OutputStream stdin;
    private static final int BG = Color.parseColor("#0D1117");
    private static final int PANEL = Color.parseColor("#161B22");
    private static final int FG = Color.parseColor("#E6EDF3");
    private static final int FG_DIM = Color.parseColor("#8B949E");
    private static final int ACCENT = Color.parseColor("#58A6FF");
    private static final int DIVIDER = Color.parseColor("#30363D");
    private final StringBuilder buffer = new StringBuilder();
    private final Object lock = new Object();

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.handler = new Handler(Looper.getMainLooper());
        buildUi();
        startShell();
    }

    private void buildUi() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(BG);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        int iDp = Ui.dp(this, 8.0f);
        linearLayout2.setPadding(iDp, iDp, iDp, iDp);
        TextView textView = new TextView(this);
        textView.setText("容器终端");
        int i = FG;
        textView.setTextColor(i);
        textView.setTextSize(16.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(action("清屏", new View.OnClickListener() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                TerminalActivity.this.lambda$buildUi$0(view);
            }
        }));
        linearLayout2.addView(action("重启", new View.OnClickListener() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                TerminalActivity.this.lambda$buildUi$1(view);
            }
        }));
        linearLayout.addView(linearLayout2);
        View view = new View(this);
        view.setBackgroundColor(DIVIDER);
        linearLayout.addView(view, new LinearLayout.LayoutParams(-1, Ui.dp(this, 0.5f)));
        TextView textView2 = new TextView(this);
        this.output = textView2;
        textView2.setTextColor(i);
        this.output.setTextSize(12.0f);
        this.output.setTypeface(Typeface.MONOSPACE);
        this.output.setTextIsSelectable(true);
        int iDp2 = Ui.dp(this, 10.0f);
        this.output.setPadding(iDp2, iDp2, iDp2, iDp2);
        ScrollView scrollView = new ScrollView(this);
        this.scroll = scrollView;
        scrollView.addView(this.output);
        linearLayout.addView(this.scroll, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(0);
        linearLayout3.setGravity(16);
        linearLayout3.setBackgroundColor(PANEL);
        int iDp3 = Ui.dp(this, 8.0f);
        linearLayout3.setPadding(iDp3, iDp3, iDp3, iDp3);
        EditText editText = new EditText(this);
        this.input = editText;
        editText.setTextColor(i);
        this.input.setHintTextColor(FG_DIM);
        this.input.setHint("输入命令，回车执行");
        this.input.setTextSize(13.0f);
        this.input.setTypeface(Typeface.MONOSPACE);
        this.input.setSingleLine(true);
        this.input.setImeOptions(4);
        this.input.setBackground(null);
        this.input.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda5
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(TextView textView3, int i2, KeyEvent keyEvent) {

                return TerminalActivity.this.lambda$buildUi$2(textView3, i2, keyEvent);
            }
        });
        linearLayout3.addView(this.input, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout3.addView(action("执行", new View.OnClickListener() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {

                TerminalActivity.this.lambda$buildUi$3(view2);
            }
        }));
        linearLayout.addView(linearLayout3);
        setContentView(linearLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$0(View view) {
        synchronized (this.lock) {
            this.buffer.setLength(0);
        }
        this.output.setText("");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$1(View view) {
        startShell();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$buildUi$2(TextView textView, int i, KeyEvent keyEvent) {
        send();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$3(View view) {
        send();
    }

    private TextView action(String str, View.OnClickListener onClickListener) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(ACCENT);
        textView.setTextSize(14.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        int iDp = Ui.dp(this, 12.0f);
        int iDp2 = Ui.dp(this, 6.0f);
        textView.setPadding(iDp, iDp2, iDp, iDp2);
        textView.setOnClickListener(onClickListener);
        return textView;
    }

    private void startShell() {
        stopShell();
        if (!ProotRunner.prootBin(this).isFile() || !ProotRunner.rootfsDir(this).isDirectory()) {
            append("容器尚未安装，请先完成初始化。\n");
            return;
        }
        try {
            Process processExecPiped = ProotRunner.execPiped(this, Arrays.asList("/bin/bash", "--noprofile", "--norc"));
            this.shell = processExecPiped;
            this.stdin = processExecPiped.getOutputStream();
            final Process process = this.shell;
            new Thread(new Runnable() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda8
                @Override // java.lang.Runnable
                public final void run() {

                    TerminalActivity.this.lambda$startShell$4(process);
                }
            }, "dsh-term-read").start();
            append("已连接容器 shell（工作目录 /home/dsh；命令卡死可点「重启」中断）\n");
        } catch (IOException e) {
            append("启动 shell 失败: " + e.getMessage() + StringUtils.LF);
        }
    }

    private void stopShell() {
        Process process = this.shell;
        if (process != null) {
            process.destroy();
            this.shell = null;
        }
        this.stdin = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:19:0x0032  */
    /* JADX WARN: Code duplicated, block: B:29:? A[RETURN, SYNTHETIC] */
    /* JADX INFO: renamed from: readLoop, reason: merged with bridge method [inline-methods] */
    public void lambda$startShell$4(Process process) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
            try {
                char[] cArr = new char[4096];
                while (true) {
                    int i = inputStreamReader.read(cArr);
                    if (i == -1) {
                        break;
                    } else {
                        append(new String(cArr, 0, i));
                    }
                }
                inputStreamReader.close();
            } catch (Throwable th) {
                try {
                    inputStreamReader.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (IOException unused) {
        }
        if (process == this.shell) {
            append("\n[会话已结束，点「重启」重新连接]\n");
        }
    }

    private void send() {
        final String string = this.input.getText().toString();
        this.input.setText("");
        final OutputStream outputStream = this.stdin;
        Process process = this.shell;
        if (process == null || outputStream == null || !process.isAlive()) {
            append("（shell 未连接，点右上角「重启」）\n");
            return;
        }
        if (!string.isEmpty()) {
            append("$ " + string + StringUtils.LF);
        }
        new Thread(new Runnable() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {

                TerminalActivity.this.lambda$send$6(outputStream, string);
            }
        }, "dsh-term-write").start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$send$6(OutputStream outputStream, String str) {
        try {
            outputStream.write((str + StringUtils.LF).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            this.handler.post(new Runnable() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {

                    TerminalActivity.this.lambda$send$5(e);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$send$5(IOException iOException) {
        append("[写入失败: " + iOException.getMessage() + "]\n");
    }

    private void append(String str) {
        synchronized (this.lock) {
            this.buffer.append(str);
            if (this.buffer.length() > MAX_BUFFER) {
                StringBuilder sb = this.buffer;
                sb.delete(0, sb.length() - KEEP_BUFFER);
                this.buffer.insert(0, "…（早期输出已截断）\n");
            }
        }
        scheduleFlush();
    }

    private void scheduleFlush() {
        if (this.flushPending) {
            return;
        }
        this.flushPending = true;
        this.handler.post(new Runnable() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {

                TerminalActivity.this.lambda$scheduleFlush$8();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$scheduleFlush$8() {
        this.flushPending = false;
        synchronized (this.lock) {
            this.output.setText(this.buffer.toString());
        }
        this.scroll.post(new Runnable() { // from class: com.dshmobile.app.TerminalActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {

                TerminalActivity.this.lambda$scheduleFlush$7();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$scheduleFlush$7() {
        this.scroll.fullScroll(130);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        stopShell();
        super.onDestroy();
    }
}
