package com.dshmobile.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/* JADX INFO: loaded from: classes.dex */
public class FileManagerActivity extends Activity {
    private static final int REQ_IMPORT = 41;
    private ArrayAdapter<String> adapter;
    private File current;
    private TextView pathView;
    private File rootfs;
    private final List<File> rows = new ArrayList();

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        File fileRootfsDir = ProotRunner.rootfsDir(this);
        this.rootfs = fileRootfsDir;
        if (!fileRootfsDir.isDirectory()) {
            Toast.makeText(this, "容器尚未安装，请先完成初始化", 1).show();
            finish();
            return;
        }
        File fileHomeDir = ProotRunner.homeDir(this);
        if (!fileHomeDir.isDirectory()) {
            fileHomeDir = this.rootfs;
        }
        this.current = fileHomeDir;
        buildUi();
        refresh();
    }

    private void buildUi() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(Ui.bgSoft(this));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        int iDp = Ui.dp(this, 12.0f);
        linearLayout2.setPadding(iDp, Ui.dp(this, 10.0f), iDp, Ui.dp(this, 4.0f));
        linearLayout2.setBackgroundColor(Ui.bg(this));
        TextView textViewTitle = Ui.title(this, "文件管理器");
        textViewTitle.setTextSize(18.0f);
        linearLayout2.addView(textViewTitle);
        TextView textViewHint = Ui.hint(this, "");
        this.pathView = textViewHint;
        textViewHint.setSingleLine(true);
        this.pathView.setEllipsize(TextUtils.TruncateAt.START);
        linearLayout2.addView(this.pathView);
        linearLayout.addView(linearLayout2);
        this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) { // from class: com.dshmobile.app.FileManagerActivity.1
            @Override // android.widget.ArrayAdapter, android.widget.Adapter
            public View getView(int i, View view, ViewGroup viewGroup) {
                View view2 = super.getView(i, view, viewGroup);
                TextView textView = (TextView) view2.findViewById(android.R.id.text1);
                textView.setTextColor(Ui.text(view2.getContext()));
                textView.setTextSize(15.0f);
                textView.setPadding(Ui.dp(FileManagerActivity.this, 16.0f), Ui.dp(FileManagerActivity.this, 12.0f), Ui.dp(FileManagerActivity.this, 16.0f), Ui.dp(FileManagerActivity.this, 12.0f));
                return view2;
            }
        };
        ListView listView = new ListView(this);
        listView.setBackgroundColor(Ui.bg(this));
        listView.setDivider(null);
        listView.setAdapter((ListAdapter) this.adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda2
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {

                FileManagerActivity.this.lambda$buildUi$0(adapterView, view, i, j);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda3
            @Override // android.widget.AdapterView.OnItemLongClickListener
            public final boolean onItemLongClick(AdapterView adapterView, View view, int i, long j) {

                return FileManagerActivity.this.lambda$buildUi$1(adapterView, view, i, j);
            }
        });
        linearLayout.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(1);
        linearLayout3.setBackgroundColor(Ui.bg(this));
        linearLayout3.setPadding(iDp, iDp, iDp, iDp);
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(0);
        Button buttonPrimaryButton = Ui.primaryButton(this, "导入到当前目录");
        buttonPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                FileManagerActivity.this.lambda$buildUi$2(view);
            }
        });
        linearLayout4.addView(buttonPrimaryButton, new LinearLayout.LayoutParams(0, Ui.dp(this, 48.0f), 1.0f));
        Button buttonOutlineButton = Ui.outlineButton(this, "新建文件夹");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 48.0f), 1.0f);
        layoutParams.leftMargin = Ui.dp(this, 12.0f);
        buttonOutlineButton.setLayoutParams(layoutParams);
        buttonOutlineButton.setOnClickListener(new View.OnClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {

                FileManagerActivity.this.lambda$buildUi$3(view);
            }
        });
        linearLayout4.addView(buttonOutlineButton);
        linearLayout3.addView(linearLayout4);
        TextView textViewHint2 = Ui.hint(this, "点文件：导出到 /mnt/shared（/sdcard/dsh-shared）；长按：删除");
        textViewHint2.setGravity(17);
        LinearLayout.LayoutParams layoutParamsMatchWrap = Ui.matchWrap();
        layoutParamsMatchWrap.topMargin = Ui.dp(this, 8.0f);
        linearLayout3.addView(textViewHint2, layoutParamsMatchWrap);
        linearLayout.addView(linearLayout3);
        setContentView(linearLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$0(AdapterView adapterView, View view, int i, long j) {
        onTap(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$buildUi$1(AdapterView adapterView, View view, int i, long j) {
        return onLongTap(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$2(View view) {
        pickImport();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$3(View view) {
        mkdirDialog();
    }

    private String containerPath(File file) {
        String absolutePath = this.rootfs.getAbsolutePath();
        String absolutePath2 = file.getAbsolutePath();
        if (absolutePath2.equals(absolutePath)) {
            return "/";
        }
        return absolutePath2.startsWith(new StringBuilder().append(absolutePath).append("/").toString()) ? absolutePath2.substring(absolutePath.length()) : absolutePath2;
    }

    private void refresh() {
        String str;
        this.pathView.setText(containerPath(this.current));
        this.rows.clear();
        this.adapter.clear();
        if (!this.current.equals(this.rootfs)) {
            this.rows.add(null);
            this.adapter.add("..  （返回上级）");
        }
        File[] fileArrListFiles = this.current.listFiles();
        if (fileArrListFiles != null) {
            Arrays.sort(fileArrListFiles, Comparator.comparing(new Function() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda7
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return Boolean.valueOf(((File) obj).isFile());
                }
            }).thenComparing(new Function() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda8
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return ((File) obj).getName().toLowerCase();
                }
            }));
            for (File file : fileArrListFiles) {
                this.rows.add(file);
                ArrayAdapter<String> arrayAdapter = this.adapter;
                if (file.isDirectory()) {
                    str = "📁 " + file.getName() + "/";
                } else {
                    str = "📄 " + file.getName() + "    " + sizeText(file.length());
                }
                arrayAdapter.add(str);
            }
        }
        this.adapter.notifyDataSetChanged();
    }

    private static String sizeText(long j) {
        if (j < FileUtils.ONE_KB) {
            return j + " B";
        }
        if (j < FileUtils.ONE_MB) {
            return (j / FileUtils.ONE_KB) + " KB";
        }
        if (j < FileUtils.ONE_GB) {
            return String.format("%.1f MB", Double.valueOf(j / 1048576.0d));
        }
        return String.format("%.2f GB", Double.valueOf(j / 1.073741824E9d));
    }

    private void onTap(int i) {
        final File file = this.rows.get(i);
        if (file == null) {
            this.current = this.current.getParentFile();
            refresh();
        } else if (file.isDirectory()) {
            this.current = file;
            refresh();
        } else {
            new AlertDialog.Builder(this).setTitle(file.getName()).setItems(new String[]{"导出到 /mnt/shared", "删除"}, new DialogInterface.OnClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda0
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i2) {

                    FileManagerActivity.this.lambda$onTap$5(file, dialogInterface, i2);
                }
            }).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onTap$5(File file, DialogInterface dialogInterface, int i) {
        if (i == 0) {
            exportFile(file);
        } else {
            confirmDelete(file);
        }
    }

    private boolean onLongTap(int i) {
        File file = this.rows.get(i);
        if (file == null) {
            return true;
        }
        confirmDelete(file);
        return true;
    }

    private void confirmDelete(final File file) {
        new AlertDialog.Builder(this).setTitle("删除").setMessage("确定删除 " + containerPath(file) + (file.isDirectory() ? "（含全部内容）" : "") + " ？").setPositiveButton("删除", new DialogInterface.OnClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {

                FileManagerActivity.this.lambda$confirmDelete$6(file, dialogInterface, i);
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmDelete$6(File file, DialogInterface dialogInterface, int i) {
        BootstrapInstaller.deleteRecursively(file);
        refresh();
    }

    private void exportFile(File file) {
        File fileUniqueName = uniqueName(ProotRunner.sharedDir(), file.getName());
        try {
            copyFile(file, fileUniqueName);
            Toast.makeText(this, "已导出到 " + fileUniqueName.getAbsolutePath(), 1).show();
        } catch (IOException e) {
            Toast.makeText(this, "导出失败：" + e.getMessage() + "\n可到设置开启「所有文件访问权限」后重试", 1).show();
        }
    }

    private static File uniqueName(File file, String str) {
        File file2 = new File(file, str);
        if (!file2.exists()) {
            return file2;
        }
        int iLastIndexOf = str.lastIndexOf(46);
        String strSubstring = iLastIndexOf > 0 ? str.substring(0, iLastIndexOf) : str;
        String strSubstring2 = iLastIndexOf > 0 ? str.substring(iLastIndexOf) : "";
        int i = 1;
        while (true) {
            File file3 = new File(file, strSubstring + "(" + i + ")" + strSubstring2);
            if (!file3.exists()) {
                return file3;
            }
            i++;
        }
    }

    /* JADX WARN: Code duplicated, block: B:30:0x0035 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private static void copyFile(File file, File file2) throws IOException {
        InputStream inputStreamNewInputStream = Files.newInputStream(file.toPath(), new OpenOption[0]);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            try {
                byte[] bArr = new byte[8];
                while (true) {
                    int i = inputStreamNewInputStream.read(bArr);
                    if (i == -1) {
                        break;
                    } else {
                        fileOutputStream.write(bArr, 0, i);
                    }
                }
                fileOutputStream.close();
                if (inputStreamNewInputStream != null) {
                    inputStreamNewInputStream.close();
                }
            } catch (Throwable th2) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th3) {
                    th2.addSuppressed(th3);
                }
                throw th2;
            }
        } catch (Throwable th4) {
            if (inputStreamNewInputStream != null) {
                inputStreamNewInputStream.close();
            }
            throw th4;
        }
    }

    private void pickImport() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("*/*");
        intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        startActivityForResult(intent, REQ_IMPORT);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == REQ_IMPORT && i2 == -1 && intent != null) {
            ArrayList arrayList = new ArrayList();
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                for (int i3 = 0; i3 < clipData.getItemCount(); i3++) {
                    arrayList.add(clipData.getItemAt(i3).getUri());
                }
            } else if (intent.getData() != null) {
                arrayList.add(intent.getData());
            }
            Iterator it = arrayList.iterator();
            int i4 = 0;
            int i5 = 0;
            while (it.hasNext()) {
                try {
                    importUri((Uri) it.next());
                    i4++;
                } catch (IOException unused) {
                    i5++;
                }
            }
            refresh();
            Toast.makeText(this, "导入完成：" + i4 + " 个" + (i5 > 0 ? "，失败 " + i5 + " 个" : ""), 0).show();
        }
    }

    private void importUri(Uri uri) throws IOException {
        File fileUniqueName = uniqueName(this.current, displayName(uri));
        InputStream inputStreamOpenInputStream = getContentResolver().openInputStream(uri);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileUniqueName);
            try {
                if (inputStreamOpenInputStream == null) {
                    throw new IOException("无法读取所选文件");
                }
                byte[] bArr = new byte[8];
                while (true) {
                    int i = inputStreamOpenInputStream.read(bArr);
                    if (i == -1) {
                        break;
                    } else {
                        fileOutputStream.write(bArr, 0, i);
                    }
                }
                fileOutputStream.close();
                if (inputStreamOpenInputStream != null) {
                    inputStreamOpenInputStream.close();
                }
            } catch (Throwable th) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            if (inputStreamOpenInputStream != null) {
                try {
                    inputStreamOpenInputStream.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    /* JADX WARN: Code duplicated, block: B:25:0x003f  */
    private String displayName(Uri uri) {
        String name = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("_display_name");
                if (columnIndex >= 0) {
                    name = cursor.getString(columnIndex);
                }
            }
        } catch (Exception unused) {
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception unused2) {
                }
            }
        }
        if (name == null || name.isEmpty()) {
            name = "import-" + System.currentTimeMillis();
        }
        return name.replace(IOUtils.DIR_SEPARATOR_UNIX, '_');
    }

    private void mkdirDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("文件夹名");
        editText.setSingleLine(true);
        int iDp = Ui.dp(this, 16.0f);
        int i = iDp / 2;
        editText.setPadding(iDp, i, iDp, i);
        new AlertDialog.Builder(this).setTitle("新建文件夹").setView(editText).setPositiveButton("创建", new DialogInterface.OnClickListener() { // from class: com.dshmobile.app.FileManagerActivity$$ExternalSyntheticLambda6
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i2) {

                FileManagerActivity.this.lambda$mkdirDialog$7(editText, dialogInterface, i2);
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$mkdirDialog$7(EditText editText, DialogInterface dialogInterface, int i) {
        String strReplace = editText.getText().toString().trim().replace("/", "");
        if (strReplace.isEmpty()) {
            return;
        }
        File file = new File(this.current, strReplace);
        if (file.exists() || !file.mkdirs()) {
            Toast.makeText(this, "创建失败（已存在或无权限）", 0).show();
        }
        refresh();
    }
}
