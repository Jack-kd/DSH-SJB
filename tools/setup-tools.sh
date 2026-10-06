#!/usr/bin/env bash
# ============================================================
# DSH Android 构建工具一键安装脚本
#   自动安装:OpenJDK 17 + Android cmdline-tools + Gradle 8.13
#   支持架构:x86_64 / aarch64(自动识别,走国内镜像,下载快)
# 用法:
#   bash setup-tools.sh            # 装到 ~/tools
#   bash setup-tools.sh /opt/tools # 装到指定目录
# 装完:source ~/tools/env.sh 即可使用 java / javac / sdkmanager
# ============================================================
set -euo pipefail

TOOLS_DIR="${1:-$HOME/tools}"
ARCH="$(uname -m)"
JDK_VER_DIR="jdk-17.0.2"
GRADLE_VER="8.13"
GRADLE_HASH="6456429c55b5e0d05d95fc36f9966d59"  # 固定值:distributionUrl 的 md5

echo "==> 目标目录: $TOOLS_DIR (架构: $ARCH)"

case "$ARCH" in
  x86_64|amd64)  JDK_URL="https://mirrors.huaweicloud.com/openjdk/17.0.2/openjdk-17.0.2_linux-x64_bin.tar.gz" ;;
  aarch64|arm64) JDK_URL="https://mirrors.huaweicloud.com/openjdk/17.0.2/openjdk-17.0.2_linux-aarch64_bin.tar.gz" ;;
  *) echo "不支持的架构: $ARCH(目前仅 x86_64 / aarch64)"; exit 1 ;;
esac

# tar 的 --hard-dereference 是 GNU 专属,macOS 的 bsdtar 不支持则忽略
TAR_HD=""; tar --hard-dereference --version >/dev/null 2>&1 && TAR_HD="--hard-dereference"

# ---------- 1. JDK ----------
if [ ! -x "$TOOLS_DIR/jdk17/bin/java" ]; then
  echo "==> 下载 JDK 17 ($ARCH) ..."
  curl -fSL --retry 3 -o /tmp/jdk17.tar.gz "$JDK_URL"
  mkdir -p "$TOOLS_DIR"
  tar -C "$TOOLS_DIR" -xzf /tmp/jdk17.tar.gz $TAR_HD
  [ -d "$TOOLS_DIR/$JDK_VER_DIR" ] && mv "$TOOLS_DIR/$JDK_VER_DIR" "$TOOLS_DIR/jdk17"
  rm -f /tmp/jdk17.tar.gz
else
  echo "==> JDK 已存在,跳过"
fi

# ---------- 2. Android cmdline-tools ----------
if [ ! -x "$TOOLS_DIR/android-sdk/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "==> 下载 Android cmdline-tools ..."
  curl -fSL --retry 3 -o /tmp/cmdtools.zip \
    "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  rm -rf /tmp/cmdtools && mkdir -p /tmp/cmdtools
  unzip -q /tmp/cmdtools.zip -d /tmp/cmdtools
  mkdir -p "$TOOLS_DIR/android-sdk/cmdline-tools/latest"
  mv /tmp/cmdtools/cmdline-tools/* "$TOOLS_DIR/android-sdk/cmdline-tools/latest/"
  rm -f /tmp/cmdtools.zip && rm -rf /tmp/cmdtools
else
  echo "==> cmdline-tools 已存在,跳过"
fi

# ---------- 3. Gradle 8.13(预置进 wrapper 缓存,以后 gradle 构建免下载) ----------
DIST="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VER}-bin/$GRADLE_HASH"
if [ ! -d "$DIST/gradle-$GRADLE_VER" ]; then
  echo "==> 下载 Gradle $GRADLE_VER ..."
  mkdir -p "$DIST"
  curl -fSL --retry 3 -o "$DIST/gradle-${GRADLE_VER}-bin.zip" \
    "https://mirrors.huaweicloud.com/gradle/gradle-${GRADLE_VER}-bin.zip" \
    || curl -fSL --retry 3 -o "$DIST/gradle-${GRADLE_VER}-bin.zip" \
         "https://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip"
  (cd "$DIST" && unzip -q "gradle-${GRADLE_VER}-bin.zip" && touch "gradle-${GRADLE_VER}-bin.zip.ok")
else
  echo "==> Gradle $GRADLE_VER 已预置,跳过"
fi

# ---------- 4. 权限(Android 存储会抹执行位,必须补) ----------
chmod -R a+rx "$TOOLS_DIR/jdk17" "$TOOLS_DIR/android-sdk" 2>/dev/null || true

# ---------- 5. 环境加载脚本(自动发现组件,幂等) ----------
cat > "$TOOLS_DIR/env.sh" <<'EOF'
# DSH 全局工具环境(自动发现组件,由 setup-tools.sh / setup-reverse-tools.sh 生成)
TOOLS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# --- JDK ---
export JAVA_HOME="$TOOLS_DIR/jdk17"
export PATH="$JAVA_HOME/bin:$PATH"

# --- Android SDK ---
export ANDROID_HOME="$TOOLS_DIR/android-sdk"
export ANDROID_SDK_ROOT="$TOOLS_DIR/android-sdk"
[ -d "$TOOLS_DIR/android-sdk/cmdline-tools/latest/bin" ] && export PATH="$TOOLS_DIR/android-sdk/cmdline-tools/latest/bin:$PATH"

# --- jadx 反编译工具 ---
[ -x "$TOOLS_DIR/jadx/bin/jadx" ] && export PATH="$TOOLS_DIR/jadx/bin:$PATH"

# 反编译快捷命令:decompile app.apk → 输出到 out/
decompile() { jadx -d out "$1"; }

echo "[tools] JDK: $JAVA_HOME ($("$JAVA_HOME/bin/java" -version 2>&1 | head -1))"
[ -x "$TOOLS_DIR/jadx/bin/jadx" ] && echo "[tools] jadx: $("$TOOLS_DIR/jadx/bin/jadx" --version 2>/dev/null)"
[ -d "$TOOLS_DIR/android-sdk/cmdline-tools/latest/bin" ] && echo "[tools] AndroidSDK: $ANDROID_SDK_ROOT"
EOF

# ---------- 6. 初始化 SDK:接受许可 + 装 platform 36 ----------
export JAVA_HOME="$TOOLS_DIR/jdk17"
export ANDROID_SDK_ROOT="$TOOLS_DIR/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
if [ ! -f "$ANDROID_SDK_ROOT/platforms/android-36/android.jar" ]; then
  echo "==> 接受 SDK 许可并安装 platform android-36 ..."
  yes | sdkmanager --licenses >/dev/null 2>&1 || true
  sdkmanager "platforms;android-36" >/dev/null 2>&1 || true
fi

echo
echo "=============================================="
echo " 安装完成!"
echo " 用法: source $TOOLS_DIR/env.sh"
echo " 之后: java / javac / sdkmanager / gradle 均可直接使用"
echo " 注意:完整打包 APK 请在 GitHub Actions(x86 runner)上执行"
echo "=============================================="
