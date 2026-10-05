package com.dshmobile.app;

import android.content.Context;
import java.io.File;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public final class NodePtyFixer {
    private static final String NODE_GYP = "/opt/node/lib/node_modules/npm/node_modules/node-gyp/bin/node-gyp.js";
    private static final String PTY_DIR = "opt/node/lib/node_modules/@deepseek-ai/dsh/node_modules/node-pty";

    private NodePtyFixer() {
    }

    public static boolean needsFix(File file) {
        File file2 = new File(file, PTY_DIR);
        return (!file2.isDirectory() || new File(file2, "build/Release/pty.node").isFile() || new File(file2, "build/Debug/pty.node").isFile() || new File(file2, "prebuilds/linux-arm64/pty.node").isFile()) ? false : true;
    }

    public static boolean fix(Context context, File file) {
        File fileRootfsDir = ProotRunner.rootfsDir(context);
        attempt(context, "--nodedir=/opt/node", file);
        if (!needsFix(fileRootfsDir)) {
            return true;
        }
        attempt(context, "--dist-url=" + Prefs.of(context).getNodeMirror(), file);
        return !needsFix(fileRootfsDir);
    }

    private static void attempt(Context context, String str, File file) {
        try {
            ProotRunner.exec(context, Arrays.asList("/bin/bash", "-c", "cd /opt/node/lib/node_modules/@deepseek-ai/dsh/node_modules/node-pty && exec /opt/node/bin/node /opt/node/lib/node_modules/npm/node_modules/node-gyp/bin/node-gyp.js rebuild " + str), file).waitFor();
        } catch (Exception unused) {
        }
    }
}
