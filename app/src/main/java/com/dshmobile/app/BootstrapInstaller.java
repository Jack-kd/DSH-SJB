package com.dshmobile.app;

import android.content.Context;
import android.os.Process;
import android.system.Os;
import android.system.OsConstants;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: classes.dex */
public final class BootstrapInstaller {
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String LIBANDROID_SHMEM_POOL = "https://mirrors.ustc.edu.cn/termux/apt/termux-main/pool/main/liba/libandroid-shmem/";
    private static final String LIBTALLOC_POOL = "https://mirrors.ustc.edu.cn/termux/apt/termux-main/pool/main/libt/libtalloc/";
    private static final String NODE_SERIES = "latest-v22.x";
    private static final String PROOT_POOL = "https://mirrors.ustc.edu.cn/termux/apt/termux-main/pool/main/p/proot/";
    private static final int READ_TIMEOUT = 60000;
    private static final String TERMUX_POOL = "https://mirrors.ustc.edu.cn/termux/apt/termux-main/pool/main/";
    private volatile boolean cancelled;
    private final Context ctx;
    private final Listener listener;
    private final Prefs prefs;

    public interface Listener {
        void onDone(boolean z, String str);

        void onLog(String str);

        void onStage(String str, int i);
    }

    public BootstrapInstaller(Context context, Listener listener) {
        this.ctx = context.getApplicationContext();
        this.prefs = Prefs.of(context);
        this.listener = listener;
    }

    public void cancel() {
        this.cancelled = true;
    }

    private void stage(String str, int i) {
        this.listener.onStage(str, i);
    }

    private void log(String str) {
        this.listener.onLog(str);
    }

    private void checkCancelled() throws IOException {
        if (this.cancelled) {
            throw new IOException("已取消");
        }
    }

    public void run() throws Throwable {
        try {
            File fileBaseDir = ProotRunner.baseDir(this.ctx);
            File file = new File(fileBaseDir, "dl");
            file.mkdirs();
            File fileRootfsDir = ProotRunner.rootfsDir(this.ctx);
            long jSysconf = Os.sysconf(OsConstants._SC_PAGESIZE);
            log("宿主内核页大小: " + jSysconf + " 字节");
            if (jSysconf == 16384) {
                log("⚠ 这是 16KB 页设备：Ubuntu rootfs 与 Node.js 官方二进制为 4KB 对齐，可能无法执行（Exec format error）；proot 本体用 Termux 新版构建（16KB 对齐）不受影响。");
            }
            File file2 = new File(file, "ubuntu-base.tar.gz");
            if (new File(fileRootfsDir, "bin/bash").isFile()) {
                log("rootfs 已存在，跳过");
            } else {
                stage("下载 Ubuntu rootfs", 2);
                download(this.prefs.getRootfsUrl(), file2, 2, 25);
                checkCancelled();
                stage("解压 rootfs", 26);
                deleteRecursively(fileRootfsDir);
                fileRootfsDir.mkdirs();
                extractTar(file2, fileRootfsDir, true, 0);
                log("rootfs 解压完成");
            }
            File fileProotBin = ProotRunner.prootBin(this.ctx);
            File file3 = new File(fileBaseDir, "loader");
            if (fileProotBin.isFile() && file3.isFile()) {
                log("proot 已存在，跳过");
            } else {
                stage("下载 proot", 32);
                String strResolveTermuxDeb = resolveTermuxDeb(PROOT_POOL, "proot_");
                log("proot 包: " + strResolveTermuxDeb);
                File file4 = new File(file, "proot.deb");
                download(strResolveTermuxDeb, file4, 32, 40);
                checkCancelled();
                stage("提取 proot", 41);
                extractProotFromDeb(file4, fileBaseDir);
                fileProotBin.setExecutable(true, true);
                file3.setExecutable(true, true);
                new File(fileBaseDir, "loader32").setExecutable(true, true);
                log("proot 与 loader 就绪");
            }
            if (jSysconf == 16384) {
                long jElfMaxAlign = elfMaxAlign(fileProotBin);
                log("proot ELF 对齐: " + jElfMaxAlign);
                if (jElfMaxAlign >= 0 && jElfMaxAlign < 16384) {
                    log("⚠ proot 二进制未按 16KB 对齐，在此设备上无法运行，请反馈此日志。");
                }
            }
            File file5 = new File(fileBaseDir, "lib");
            if (new File(file5, "libtalloc.so.2").isFile()) {
                log("proot 依赖库已存在，跳过");
            } else {
                stage("下载 proot 依赖库", 42);
                file5.mkdirs();
                String strResolveTermuxDeb2 = resolveTermuxDeb(LIBTALLOC_POOL, "libtalloc_");
                log("libtalloc 包: " + strResolveTermuxDeb2);
                File file6 = new File(file, "libtalloc.deb");
                download(strResolveTermuxDeb2, file6, 42, 43);
                extractLibsFromDeb(file6, file5);
                checkCancelled();
                String strResolveTermuxDeb3 = resolveTermuxDeb(LIBANDROID_SHMEM_POOL, "libandroid-shmem_");
                log("libandroid-shmem 包: " + strResolveTermuxDeb3);
                File file7 = new File(file, "libandroid-shmem.deb");
                download(strResolveTermuxDeb3, file7, 43, 44);
                extractLibsFromDeb(file7, file5);
                log("proot 依赖库就绪");
            }
            if (new File(fileRootfsDir, "opt/node/bin/node").isFile()) {
                log("Node.js 已存在，跳过");
            } else {
                stage("下载 Node.js", 45);
                String strResolveNodeUrl = resolveNodeUrl();
                log("Node 包: " + strResolveNodeUrl);
                File file8 = new File(file, "node.tar.xz");
                download(strResolveNodeUrl, file8, 45, 65);
                checkCancelled();
                stage("解压 Node.js", 66);
                deleteRecursively(new File(fileRootfsDir, "opt/node"));
                new File(fileRootfsDir, "opt/node").mkdirs();
                extractTar(file8, new File(fileRootfsDir, "opt/node"), false, 1);
                log("Node.js 就绪");
            }
            stage("配置容器", 72);
            writeContainerConfig(fileRootfsDir);
            if (new File(fileRootfsDir, "usr/bin/g++").isFile()) {
                log("编译工具链已存在，跳过");
            } else {
                stage("安装编译工具链", 74);
                killStaleAptProcesses();
                runInContainer(Arrays.asList("/usr/bin/apt-get", "update"), 74, 78);
                checkCancelled();
                try {
                    runInContainer(Arrays.asList("/usr/bin/dpkg", "--configure", "-a"), 78, 78);
                } catch (Exception unused) {
                    log("dpkg 状态修复未完成（忽略，继续安装）");
                }
                runInContainer(Arrays.asList("/usr/bin/apt-get", "-o", "DPkg::Lock::Timeout=180", "install", "-y", "--no-install-recommends", "python3", "make", "g++", "ca-certificates"), 78, 82);
            }
            if (new File(fileRootfsDir, "opt/node/bin/dsh").isFile()) {
                log("dsh 已安装，跳过");
            } else {
                stage("安装 DeepSeek Harness", 83);
                File[] fileArrListFiles = new File(fileRootfsDir, "opt/node/lib/node_modules/@deepseek-ai").listFiles();
                if (fileArrListFiles != null) {
                    for (File file9 : fileArrListFiles) {
                        if (file9.getName().equals("dsh") || file9.getName().startsWith(".dsh-")) {
                            deleteRecursively(file9);
                        }
                    }
                }
                runInContainer(Arrays.asList("/opt/node/bin/npm", "config", "set", "registry", this.prefs.getNpmRegistry()), 83, 85);
                checkCancelled();
                runInContainer(Arrays.asList("/opt/node/bin/npm", "install", "-g", "@deepseek-ai/dsh"), 85, 96);
            }
            if (NodePtyFixer.needsFix(fileRootfsDir)) {
                stage("编译 node-pty 原生模块", 97);
                log("node-pty 缺少 pty.node，正在容器内重建…");
                if (!NodePtyFixer.fix(this.ctx, new File(ProotRunner.baseDir(this.ctx), "install.log"))) {
                    throw new IOException("node-pty 原生模块重建失败，请到设置查看日志");
                }
                log("node-pty 原生模块就绪");
            }
            checkCancelled();
            stage("完成", 100);
            this.prefs.setSetupDone(true);
            this.listener.onDone(true, null);
        } catch (Exception e) {
            log("安装失败: " + e.getMessage());
            this.listener.onDone(false, e.getMessage());
        }
    }

    private void writeContainerConfig(File file) throws IOException {
        File file2 = new File(file, "etc");
        file2.mkdirs();
        Files.write(new File(file2, "resolv.conf").toPath(), "nameserver 223.5.5.5\nnameserver 8.8.8.8\n".getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        File file3 = new File(file2, "apt");
        file3.mkdirs();
        Files.write(new File(file3, "sources.list").toPath(), "deb http://mirrors.ustc.edu.cn/ubuntu-ports jammy main restricted universe multiverse\ndeb http://mirrors.ustc.edu.cn/ubuntu-ports jammy-updates main restricted universe multiverse\ndeb http://mirrors.ustc.edu.cn/ubuntu-ports jammy-security main restricted universe multiverse\n".getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        new File(file, "mnt/sd").mkdirs();
        new File(file, "mnt/shared").mkdirs();
        new File(file, "root").mkdirs();
        log("已写入 resolv.conf、apt 源与挂载点");
    }

    private void runInContainer(List<String> list, int i, int i2) throws InterruptedException, IOException {
        File file = new File(ProotRunner.baseDir(this.ctx), "install.log");
        ProcessBuilder processBuilder = new ProcessBuilder(ProotRunner.buildCommand(this.ctx, list));
        ProotRunner.applyEnv(this.ctx, processBuilder);
        processBuilder.redirectErrorStream(true);
        java.lang.Process processStart = processBuilder.start();
        InputStream inputStream = processStart.getInputStream();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            try {
                byte[] bArr = new byte[4096];
                StringBuilder sb = new StringBuilder();
                long j = 0;
                int i3 = i;
                while (true) {
                    int i4 = inputStream.read(bArr);
                    if (i4 == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, i4);
                    fileOutputStream.flush();
                    for (int i5 = 0; i5 < i4; i5++) {
                        char c = (char) bArr[i5];
                        if (c == '\n' || c == '\r') {
                            if (sb.length() > 0) {
                                log(sb.toString());
                                sb.setLength(0);
                            }
                        } else if (sb.length() < 300) {
                            sb.append(c);
                        }
                    }
                    long jCurrentTimeMillis = System.currentTimeMillis();
                    if (jCurrentTimeMillis - j > 2000 && i3 < i2) {
                        i3++;
                        stage(null, i3);
                        j = jCurrentTimeMillis;
                    }
                }
                fileOutputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }
                int iWaitFor = processStart.waitFor();
                if (iWaitFor != 0) {
                    throw new IOException("容器命令失败(" + iWaitFor + "): " + list);
                }
            } catch (Throwable th) {
                try {
                    fileOutputStream.close();
                    throw th;
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                    throw th;
                }
            }
        } catch (Throwable th3) {
            if (inputStream == null) {
                throw th3;
            }
            try {
                inputStream.close();
                throw th3;
            } catch (Throwable th4) {
                th3.addSuppressed(th4);
                throw th3;
            }
        }
    }

    private void killStaleAptProcesses() {
        String procFile;
        String procFile2;
        File[] fileArrListFiles = new File("/proc").listFiles();
        if (fileArrListFiles == null) {
            return;
        }
        int iMyUid = Process.myUid();
        int iMyPid = Process.myPid();
        boolean z = false;
        for (File file : fileArrListFiles) {
            try {
                int i = Integer.parseInt(file.getName());
                if (i != iMyPid && (procFile = readProcFile(new File(file, "status"))) != null && parseUid(procFile) == iMyUid && (procFile2 = readProcFile(new File(file, "cmdline"))) != null && (procFile2.contains("apt-get") || procFile2.contains("/dpkg") || procFile2.contains("unattended-upgrade"))) {
                    log("清理残留的包管理进程 (pid " + i + ")");
                    Process.killProcess(i);
                    z = true;
                }
            } catch (Exception unused) {
            }
        }
        if (z) {
            try {
                Thread.sleep(800L);
            } catch (InterruptedException unused2) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String readProcFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                String strReplace = new String(readAll(fileInputStream), StandardCharsets.UTF_8).replace((char) 0, ' ');
                fileInputStream.close();
                return strReplace;
            } catch (Throwable th) {
                try {
                    fileInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (IOException unused) {
            return null;
        }
    }

    private static int parseUid(String str) {
        for (String str2 : str.split(StringUtils.LF)) {
            if (str2.startsWith("Uid:")) {
                String[] strArrSplit = str2.trim().split("\\s+");
                if (strArrSplit.length >= 2) {
                    try {
                        return Integer.parseInt(strArrSplit[1]);
                    } catch (NumberFormatException unused) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r23v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r23v1 */
    /* JADX WARN: Type inference failed for: r23v14 */
    /* JADX WARN: Type inference failed for: r23v15 */
    /* JADX WARN: Type inference failed for: r23v2, types: [java.net.HttpURLConnection] */
    /* JADX WARN: Type inference failed for: r23v3 */
    /* JADX WARN: Type inference failed for: r23v4 */
    /* JADX WARN: Type inference failed for: r23v5 */
    /* JADX WARN: Type inference failed for: r23v6 */
    /* JADX WARN: Type inference failed for: r23v8 */
    private void download(String str, File file, int i, int i2) throws Throwable {
        checkCancelled();
        File part = new File(file.getParentFile(), file.getName() + ".part");
        HttpURLConnection connection = open(str);
        try {
            long total = connection.getContentLengthLong();
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            FileOutputStream out = new FileOutputStream(part);
            try {
                byte[] buf = new byte[8];
                long done = 0;
                long lastTick = 0;
                while (true) {
                    int n = in.read(buf);
                    if (n == -1) {
                        break;
                    }
                    checkCancelled();
                    out.write(buf, 0, n);
                    done += (long) n;
                    long now = System.currentTimeMillis();
                    if (now - lastTick > 500) {
                        int pct = i;
                        if (total > 0) {
                            pct = i + (int) (((long) (i2 - i)) * done / total);
                        }
                        stage(null, Math.min(pct, i2));
                        log(String.format("已下载 %.1f MB", Double.valueOf(done / 1048576.0d)));
                        lastTick = now;
                    }
                }
            } finally {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            if (!part.renameTo(file)) {
                throw new IOException("无法写入 " + file);
            }
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection open(String str) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
        httpURLConnection.setReadTimeout(READ_TIMEOUT);
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestProperty("User-Agent", "dsh-mobile/1.0");
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode < 400) {
            return httpURLConnection;
        }
        throw new IOException("HTTP " + responseCode + ": " + str);
    }

    private String fetchText(String str) throws IOException {
        HttpURLConnection connection = open(str);
        try {
            InputStream inputStream = connection.getInputStream();
            StringBuilder sb = new StringBuilder();
            try {
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = inputStream.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    sb.append(new String(bArr, 0, i, StandardCharsets.UTF_8));
                }
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            return sb.toString();
        } finally {
            connection.disconnect();
        }
    }

    private String resolveTermuxDeb(String str, String str2) throws IOException {
        Matcher matcher = Pattern.compile("href=\"(?:[^\"]*/)?(" + Pattern.quote(str2) + "[^\"]+_aarch64\\.deb)\"").matcher(fetchText(str));
        String strGroup = null;
        while (matcher.find()) {
            strGroup = matcher.group(1);
        }
        if (strGroup == null) {
            throw new IOException("未找到 " + str2 + " aarch64 包");
        }
        return str + strGroup;
    }

    private String resolveNodeUrl() throws IOException {
        String nodeMirror = this.prefs.getNodeMirror();
        if (nodeMirror.endsWith("/")) {
            nodeMirror = nodeMirror.substring(0, nodeMirror.length() - 1);
        }
        Matcher matcher = Pattern.compile("(node-v22\\.\\d+\\.\\d+)-linux-arm64\\.tar\\.xz").matcher(fetchText(nodeMirror + "/latest-v22.x/"));
        String strGroup = null;
        while (matcher.find()) {
            strGroup = matcher.group(1);
        }
        if (strGroup == null) {
            throw new IOException("未找到 Node linux-arm64 包");
        }
        return nodeMirror + "/latest-v22.x/" + strGroup + "-linux-arm64.tar.xz";
    }

    private void extractTar(File file, File file2, boolean z, int i) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(z ? new GzipCompressorInputStream(bufferedInputStream) : new XZCompressorInputStream(bufferedInputStream));
        while (true) {
            try {
                TarArchiveEntry nextEntry = tarArchiveInputStream.getNextEntry();
                if (nextEntry != null) {
                    checkCancelled();
                    String[] strArrSplit = nextEntry.getName().split("/");
                    ArrayList arrayList = new ArrayList();
                    for (String str : strArrSplit) {
                        if (!str.isEmpty() && !str.equals(".")) {
                            arrayList.add(str);
                        }
                    }
                    if (arrayList.size() > i) {
                        String strJoin = String.join("/", arrayList.subList(i, arrayList.size()));
                        if (!strJoin.isEmpty() && !strJoin.contains("..")) {
                            File file3 = new File(file2, strJoin);
                            if (nextEntry.isDirectory()) {
                                file3.mkdirs();
                            } else if (nextEntry.isSymbolicLink()) {
                                String linkName = nextEntry.getLinkName();
                                file3.getParentFile().mkdirs();
                                file3.delete();
                                try {
                                    Files.createSymbolicLink(file3.toPath(), new File(linkName).toPath(), new FileAttribute[0]);
                                } catch (Exception unused) {
                                    log("跳过符号链接: " + strJoin);
                                }
                            } else if (nextEntry.isLink()) {
                                file3.getParentFile().mkdirs();
                                File file4 = new File(file2, nextEntry.getLinkName());
                                file3.delete();
                                try {
                                    Files.createLink(file3.toPath(), file4.toPath());
                                } catch (Exception unused2) {
                                    log("跳过硬链接: " + strJoin);
                                }
                            } else {
                                file3.getParentFile().mkdirs();
                                FileOutputStream fileOutputStream = new FileOutputStream(file3);
                                try {
                                    byte[] bArr = new byte[8];
                                    while (true) {
                                        int i2 = tarArchiveInputStream.read(bArr);
                                        if (i2 == -1) {
                                            break;
                                        } else {
                                            fileOutputStream.write(bArr, 0, i2);
                                        }
                                    }
                                    fileOutputStream.close();
                                    if ((nextEntry.getMode() & 64) != 0) {
                                        file3.setExecutable(true, true);
                                    }
                                } catch (Throwable th2) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (Throwable th3) {
                                        th2.addSuppressed(th3);
                                    }
                                    throw th2;
                                }
                            }
                        }
                    }
                } else {
                    tarArchiveInputStream.close();
                    return;
                }
            } catch (Throwable th4) {
                tarArchiveInputStream.close();
                throw th4;
            }
        }
    }

    private void extractProotFromDeb(File file, File file2) throws IOException {
        boolean zEndsWith;
        byte[] all;
        InputStream gzipCompressorInputStream;
        String str;
        ArArchiveInputStream arArchiveInputStream = new ArArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
        while (true) {
            try {
                ArArchiveEntry nextEntry = arArchiveInputStream.getNextEntry();
                if (nextEntry == null) {
                    zEndsWith = true;
                    all = null;
                    break;
                } else {
                    String name = nextEntry.getName();
                    if (name.startsWith("data.tar")) {
                        zEndsWith = name.endsWith(".xz");
                        all = readAll(arArchiveInputStream);
                        break;
                    }
                }
            } catch (Throwable th) {
                try {
                    arArchiveInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        if (all == null) {
            throw new IOException("deb 中未找到 data.tar");
        }
        if (zEndsWith) {
            gzipCompressorInputStream = new XZCompressorInputStream(new ByteArrayInputStream(all));
        } else {
            gzipCompressorInputStream = new GzipCompressorInputStream(new ByteArrayInputStream(all));
        }
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream);
        int i = 0;
        while (true) {
            try {
                TarArchiveEntry nextEntry2 = tarArchiveInputStream.getNextEntry();
                if (nextEntry2 == null) {
                    break;
                }
                String name2 = nextEntry2.getName();
                if (!nextEntry2.isDirectory()) {
                    if (name2.endsWith("/bin/proot")) {
                        str = "proot";
                    } else if (name2.endsWith("/libexec/proot/loader")) {
                        str = "loader";
                    } else {
                        str = name2.endsWith("/libexec/proot/loader32") ? "loader32" : null;
                    }
                    if (str != null) {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(file2, str));
                        try {
                            byte[] bArr = new byte[8];
                            while (true) {
                                int i2 = tarArchiveInputStream.read(bArr);
                                if (i2 == -1) {
                                    break;
                                } else {
                                    fileOutputStream.write(bArr, 0, i2);
                                }
                            }
                            fileOutputStream.close();
                            i++;
                        } catch (Throwable th4) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable th5) {
                                th4.addSuppressed(th5);
                            }
                            throw th4;
                        }
                    }
                }
            } catch (Throwable th6) {
                tarArchiveInputStream.close();
                throw th6;
            }
        }
        tarArchiveInputStream.close();
        if (i == 0) {
            throw new IOException("deb 中未找到 proot 二进制");
        }
        arArchiveInputStream.close();
    }

    private void extractLibsFromDeb(File file, File file2) throws IOException {
        byte[] all;
        boolean zEndsWith;
        InputStream gzipCompressorInputStream;
        HashMap<String, byte[]> map = new HashMap<>();
        HashMap<String, String> map2 = new HashMap<>();
        ArArchiveInputStream arArchiveInputStream = new ArArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
        while (true) {
            try {
                ArArchiveEntry nextEntry = arArchiveInputStream.getNextEntry();
                if (nextEntry != null) {
                    String name = nextEntry.getName();
                    if (name.startsWith("data.tar")) {
                        zEndsWith = name.endsWith(".xz");
                        all = readAll(arArchiveInputStream);
                        break;
                    }
                } else {
                    all = null;
                    zEndsWith = true;
                    break;
                }
            } catch (Throwable th) {
                try {
                    arArchiveInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        if (all == null) {
            throw new IOException("deb 中未找到 data.tar");
        }
        if (zEndsWith) {
            gzipCompressorInputStream = new XZCompressorInputStream(new ByteArrayInputStream(all));
        } else {
            gzipCompressorInputStream = new GzipCompressorInputStream(new ByteArrayInputStream(all));
        }
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream);
        while (true) {
            try {
                TarArchiveEntry nextEntry2 = tarArchiveInputStream.getNextEntry();
                if (nextEntry2 == null) {
                    break;
                }
                String name2 = nextEntry2.getName();
                String strSubstring = name2.substring(name2.lastIndexOf(47) + 1);
                if (strSubstring.contains(".so")) {
                    if (nextEntry2.isSymbolicLink()) {
                        String linkName = nextEntry2.getLinkName();
                        map2.put(strSubstring, linkName.substring(linkName.lastIndexOf(47) + 1));
                    } else if (!nextEntry2.isDirectory()) {
                        map.put(strSubstring, readAll(tarArchiveInputStream));
                    }
                }
            } catch (Throwable th3) {
                try {
                    tarArchiveInputStream.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
                throw th3;
            }
        }
        tarArchiveInputStream.close();
        arArchiveInputStream.close();
        if (map.isEmpty()) {
            throw new IOException("deb 中未找到 .so 库");
        }
        for (Map.Entry entry : map.entrySet()) {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(file2, (String) entry.getKey()));
            try {
                fileOutputStream.write((byte[]) entry.getValue());
                fileOutputStream.close();
            } catch (Throwable th5) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th6) {
                    th5.addSuppressed(th6);
                }
                throw th5;
            }
        }
        for (Map.Entry entry2 : map2.entrySet()) {
            byte[] bArr = (byte[]) map.get(entry2.getValue());
            if (bArr != null && !map.containsKey(entry2.getKey())) {
                FileOutputStream fileOutputStream2 = new FileOutputStream(new File(file2, (String) entry2.getKey()));
                try {
                    fileOutputStream2.write(bArr);
                    fileOutputStream2.close();
                } catch (Throwable th7) {
                    try {
                        fileOutputStream2.close();
                    } catch (Throwable th8) {
                        th7.addSuppressed(th8);
                    }
                    throw th7;
                }
            }
        }
        log("已提取库: " + map.keySet());
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8];
        while (true) {
            int i = inputStream.read(bArr);
            if (i != -1) {
                byteArrayOutputStream.write(bArr, 0, i);
            } else {
                return byteArrayOutputStream.toByteArray();
            }
        }
    }

    static long elfMaxAlign(File file) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            try {
                byte[] bArr = new byte[6];
                randomAccessFile.readFully(bArr);
                if (bArr[0] == 127 && bArr[1] == 69 && bArr[2] == 76 && bArr[3] == 70 && bArr[4] == 2 && bArr[5] == 1) {
                    long leLong = readLeLong(randomAccessFile, 32L);
                    int leShort = readLeShort(randomAccessFile, 54L);
                    int leShort2 = readLeShort(randomAccessFile, 56L);
                    long j = 0;
                    for (int i = 0; i < leShort2; i++) {
                        long leLong2 = readLeLong(randomAccessFile, (((long) i) * ((long) leShort)) + leLong + 48);
                        if (leLong2 > j) {
                            j = leLong2;
                        }
                    }
                    randomAccessFile.close();
                    return j;
                }
                randomAccessFile.close();
                return -1L;
            } catch (Throwable th) {
                try {
                    randomAccessFile.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Exception unused) {
            return -1L;
        }
    }

    private static long readLeLong(RandomAccessFile randomAccessFile, long j) throws IOException {
        randomAccessFile.seek(j);
        long j2 = 0;
        for (int i = 0; i < 8; i++) {
            j2 |= (((long) randomAccessFile.read()) & 255) << (i * 8);
        }
        return j2;
    }

    private static int readLeShort(RandomAccessFile randomAccessFile, long j) throws IOException {
        randomAccessFile.seek(j);
        return ((randomAccessFile.read() & 0xFF) << 8) | (randomAccessFile.read() & 0xFF);
    }

    static void deleteRecursively(File file) {
        File[] fileArrListFiles;
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory() && (fileArrListFiles = file.listFiles()) != null) {
            for (File file2 : fileArrListFiles) {
                deleteRecursively(file2);
            }
        }
        file.delete();
    }
}
