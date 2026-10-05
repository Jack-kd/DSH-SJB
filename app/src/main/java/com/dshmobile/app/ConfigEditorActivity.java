package com.dshmobile.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

/* JADX INFO: loaded from: classes.dex */
public class ConfigEditorActivity extends Activity {
    private File configFile;
    private EditText editor;
    private TextView status;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        this.configFile = new File(ProotRunner.homeDir(this), ".dsh/settings.yaml");
        buildUi();
        loadFile();
    }

    private void buildUi() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        int iDp = Ui.dp(this, 16.0f);
        linearLayout.setPadding(iDp, iDp, iDp, iDp);
        linearLayout.setBackgroundColor(Ui.bg(this));
        linearLayout.addView(Ui.title(this, "配置文件"));
        View viewHint = Ui.hint(this, this.configFile.getAbsolutePath());
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.topMargin = Ui.dp(this, 4.0f);
        linearLayout.addView(viewHint, layoutParamsMatchWrap);
        EditText editText = new EditText(this);
        this.editor = editText;
        editText.setTypeface(Typeface.MONOSPACE);
        this.editor.setTextSize(13.0f);
        this.editor.setTextColor(Ui.text(this));
        this.editor.setGravity(8388659);
        this.editor.setHorizontallyScrolling(true);
        this.editor.setBackgroundColor(Ui.bgSoft(this));
        int iDp2 = Ui.dp(this, 12.0f);
        this.editor.setPadding(iDp2, iDp2, iDp2, iDp2);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(this.editor, new FrameLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
        layoutParams.topMargin = Ui.dp(this, 12.0f);
        linearLayout.addView(scrollView, layoutParams);
        this.status = Ui.hint(this, "");
        LinearLayout.LayoutParams layoutParamsMatchWrap2 = Ui.matchWrap();
        layoutParamsMatchWrap2.topMargin = Ui.dp(this, 8.0f);
        linearLayout.addView(this.status, layoutParamsMatchWrap2);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(8388613);
        LinearLayout.LayoutParams layoutParamsMatchWrap3 = Ui.matchWrap();
        layoutParamsMatchWrap3.topMargin = Ui.dp(this, 8.0f);
        Button buttonOutlineButton = Ui.outlineButton(this, "返回");
        buttonOutlineButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.ConfigEditorActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                ConfigEditorActivity.this.lambda$buildUi$0(view);
            }
        });
        linearLayout2.addView(buttonOutlineButton);
        Button buttonPrimaryButton = Ui.primaryButton(this, "保存");
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, Ui.dp(this, 48.0f));
        layoutParams2.leftMargin = Ui.dp(this, 16.0f);
        buttonPrimaryButton.setLayoutParams(layoutParams2);
        buttonPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.ConfigEditorActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                ConfigEditorActivity.this.lambda$buildUi$1(view);
            }
        });
        linearLayout2.addView(buttonPrimaryButton);
        linearLayout.addView(linearLayout2, layoutParamsMatchWrap3);
        setContentView(linearLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$0(View view) {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$1(View view) {
        saveFile();
    }

    private void loadFile() {
        if (!this.configFile.isFile()) {
            this.editor.setText("# dsh settings\n# 命名空间配置项见设置弹窗；保存后 dsh 自动热加载。\n");
            this.status.setText("文件尚不存在，保存时创建");
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(this.configFile);
            byte[] bArr = new byte[(int) Math.min(this.configFile.length(), FileUtils.ONE_MB)];
            int i = fileInputStream.read(bArr);
            fileInputStream.close();
            this.editor.setText(new String(bArr, 0, Math.max(i, 0), StandardCharsets.UTF_8));
        } catch (IOException e) {
            this.status.setText("读取失败：" + e.getMessage());
        }
    }

    private void saveFile() {
        try {
            File parentFile = this.configFile.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            File file = new File(this.configFile.getParentFile(), "settings.yaml.tmp");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(this.editor.getText().toString().getBytes(StandardCharsets.UTF_8));
            fileOutputStream.getFD().sync();
            fileOutputStream.close();
            if (!file.renameTo(this.configFile)) {
                throw new IOException("rename failed");
            }
            this.status.setText("已保存（dsh 自动热加载，无需重启）");
            Toast.makeText(this, "配置已保存", 0).show();
        } catch (IOException e) {
            this.status.setText("保存失败：" + e.getMessage());
        }
    }
}
