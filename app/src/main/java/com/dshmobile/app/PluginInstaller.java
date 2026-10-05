package com.dshmobile.app;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.lang3.time.DateUtils;

/* JADX INFO: loaded from: classes.dex */
public final class PluginInstaller {
    private static final Pattern SPEC = Pattern.compile("^[a-zA-Z0-9@][a-zA-Z0-9._/@:+\\-~]{0,127}$");
    private static final long TIMEOUT_MS = 600000;

    private PluginInstaller() {
    }

    public static final class Result {
        public final boolean ok;
        public final String output;

        Result(boolean z, String str) {
            this.ok = z;
            this.output = str;
        }
    }

    public static Result install(Context context, String str) {
        if (str == null || !SPEC.matcher(str).matches()) {
            return new Result(false, "无效的插件包名：" + str);
        }
        try {
            String strEnsurePnpm = ensurePnpm(context);
            if (strEnsurePnpm != null) {
                return new Result(false, strEnsurePnpm);
            }
            return run(context, Arrays.asList("dsh", "plugin", "--profile", "web", "add", str));
        } catch (Exception e) {
            return new Result(false, String.valueOf(e));
        }
    }

    private static String ensurePnpm(Context context) throws Exception {
        if (new File(ProotRunner.rootfsDir(context), "opt/node/bin/pnpm").isFile()) {
            return null;
        }
        Result resultRun = run(context, Arrays.asList("corepack", "enable"), 120000L);
        if (resultRun.ok) {
            return null;
        }
        return "pnpm 初始化失败：\n" + resultRun.output;
    }

    private static Result run(Context context, List<String> list) throws Exception {
        return run(context, list, TIMEOUT_MS);
    }

    private static Result run(Context context, List<String> list, long j) throws Exception {
        final Process processExecPiped = ProotRunner.execPiped(context, list);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Thread thread = new Thread(new Runnable() { // from class: com.dshmobile.app.PluginInstaller$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                PluginInstaller.lambda$run$0(processExecPiped, byteArrayOutputStream);
            }
        }, "dsh-plugin-install-read");
        thread.setDaemon(true);
        thread.start();
        if (!processExecPiped.waitFor(j, TimeUnit.MILLISECONDS)) {
            processExecPiped.destroy();
            return new Result(false, "安装超时（" + (j / DateUtils.MILLIS_PER_MINUTE) + " 分钟）\n" + tail(byteArrayOutputStream));
        }
        thread.join(3000L);
        return new Result(processExecPiped.exitValue() == 0, tail(byteArrayOutputStream));
    }

    static /* synthetic */ void lambda$run$0(Process process, ByteArrayOutputStream byteArrayOutputStream) {
        try {
            InputStream inputStream = process.getInputStream();
            byte[] bArr = new byte[4096];
            while (true) {
                int i = inputStream.read(bArr);
                if (i == -1) {
                    return;
                } else {
                    byteArrayOutputStream.write(bArr, 0, i);
                }
            }
        } catch (Exception unused) {
        }
    }

    private static String tail(ByteArrayOutputStream byteArrayOutputStream) {
        String str = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        return str.length() > 4000 ? str.substring(str.length() - 4000) : str;
    }
}
