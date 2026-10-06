#!/usr/bin/env bash
# ============================================================
# DSH 反编译工具链一键安装:OpenJDK 17 + jadx 1.5.6(CLI + GUI)
#   - 自动识别架构 x86_64 / aarch64(JDK 走国内镜像,jadx 走加速代理)
#   - jadx 下载后 sha256 校验,防篡改
#   - 幂等:已装则跳过,可重复执行
# 用法:
#   bash setup-reverse-tools.sh            # 装到 ~/tools
#   bash setup-reverse-tools.sh /opt/tools # 装到指定目录
# 装完:source ~/tools/env.sh
#   之后 jadx / decompile 直接可用
#   decompile app.apk   → 反编译输出到 out/ 目录
# ============================================================
set -euo pipefail

TOOLS_DIR="${1:-$HOME/tools}"
ARCH="$(uname -m)"
JDK_VER_DIR="jdk-17.0.2"
JADX_VERSION="1.5.6"
JADX_SHA256="545ea2be9c242511bc145755cf4bda2485ade42966e096f8b4d3da2a230e8974"  # jadx-1.5.6.zip

echo "==> 目标目录: $TOOLS_DIR (架构: $ARCH)"

case "$ARCH" in
  x86_64|amd64)  JDK_URL="https://mirrors.huaweicloud.com/openjdk/17.0.2/openjdk-17.0.2_linux-x64_bin.tar.gz" ;;
  aarch64|arm64) JDK_URL="https://mirrors.huaweicloud.com/openjdk/17.0.2/openjdk-17.0.2_linux-aarch64_bin.tar.gz" ;;
  *) echo "不支持的架构: $ARCH(目前仅 x86_64 / aarch64)"; exit 1 ;;
esac

TAR_HD=""; tar --hard-dereference --version >/dev/null 2>&1 && TAR_HD="--hard-dereference"

# ---------- 1. JDK(jadx 运行依赖 Java 11+,装 17) ----------
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

# ---------- 2. jadx ----------
if [ ! -x "$TOOLS_DIR/jadx/bin/jadx" ]; then
  echo "==> 下载 jadx $JADX_VERSION(加速代理 + sha256 校验) ..."
  JADX_URL="https://github.com/skylot/jadx/releases/download/v${JADX_VERSION}/jadx-${JADX_VERSION}.zip"
  ok=0
  for base in "https://gh-proxy.com/" "https://ghfast.top/" ""; do
    echo "   尝试源: ${base:-直连}github.com"
    if curl -fSL --retry 2 -o /tmp/jadx.zip "${base}${JADX_URL}" 2>/dev/null; then
      if echo "$JADX_SHA256  /tmp/jadx.zip" | sha256sum -c - >/dev/null 2>&1; then
        echo "   sha256 校验通过"; ok=1; break
      else
        echo "   sha256 校验失败,换下一个源"; rm -f /tmp/jadx.zip
      fi
    fi
  done
  [ "$ok" = "1" ] || { echo "jadx 下载失败(可手动下载后解压到 $TOOLS_DIR/jadx)"; exit 1; }

  rm -rf /tmp/jadx-extract && mkdir -p /tmp/jadx-extract "$TOOLS_DIR/jadx"
  unzip -q /tmp/jadx.zip -d /tmp/jadx-extract
  # 兼容两种 zip 布局:扁平(bin/lib 在根)或带顶层目录(jadx-1.5.6/)
  if ls -d /tmp/jadx-extract/jadx-* >/dev/null 2>&1; then
    mv /tmp/jadx-extract/jadx-*/* "$TOOLS_DIR/jadx/"
  else
    mv /tmp/jadx-extract/* "$TOOLS_DIR/jadx/"
  fi
  rm -f /tmp/jadx.zip && rm -rf /tmp/jadx-extract
else
  echo "==> jadx 已存在,跳过"
fi

# ---------- 3. 权限(Android 存储会抹执行位) ----------
chmod -R a+rx "$TOOLS_DIR/jdk17" "$TOOLS_DIR/jadx" 2>/dev/null || true

# ---------- 4. 环境加载脚本(自动发现组件,幂等) ----------
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

echo
echo "=============================================="
echo " 反编译工具链安装完成!"
echo " 用法: source $TOOLS_DIR/env.sh"
echo " 之后直接: jadx -d out app.apk   或  decompile app.apk"
echo " (jadx-gui 可执行:$TOOLS_DIR/jadx/bin/jadx-gui,需图形界面)"
echo "=============================================="
