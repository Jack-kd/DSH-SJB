# DSH 手机版(dsh-mobile)重建工程

> 由 `DSH 手机版_1.0.19.apk`(versionCode 20 / versionName 1.0.19,包名 `com.dshmobile.app`)
> 的 jadx 反编译源码**工程化重建**而来,可直接用 GitHub Actions 编译出可安装 APK。

## 这是什么

在 Android 上以 **PRoot 容器运行 DeepSeek Harness(DSH)Web 服务**、并用 WebView 承载界面的壳应用:

- `MainActivity` — WebView 加载 `http://127.0.0.1:3080/`,注入移动端适配 css/js,FAB 文件管理/终端
- `HarnessService` — 前台服务,运行 `proot -r rootfs dsh web --host 127.0.0.1 --port <port>`,异常自动重启(≤5 次)
- `BootstrapInstaller` — 首次安装流水线:下载 Ubuntu 22.04 rootfs(USTC 镜像)→ proot → Node.js v22 → apt 装编译工具链 → `npm i -g @deepseek-ai/dsh`
- `SettingsActivity` / `ConfigEditorActivity` / `TerminalActivity` / `FileManagerActivity` — 设置 / `~/.dsh/settings.yaml` 编辑器 / 容器终端 / 文件管理

首次安装约需下载 400MB;**无任何隐私收集,唯一网络行为是下载镜像与运行本地 Web 服务**。

## 内容结构

```
dsh-mobile/
├── .github/workflows/android.yml   GitHub Actions 编译工作流(出 debug + release APK)
├── app/
│   ├── build.gradle.kts            应用模块(compileSdk 36 / minSdk 26 / targetSdk 28)
│   └── src/main/
│       ├── AndroidManifest.xml     应用清单
│       ├── java/com/dshmobile/app/ 13 个应用类(已修复为可编译)
│       ├── res/                    颜色/样式/字符串/XML drawable
│       └── assets/                 inject.js + mobile.css(移动端 Web UI 适配,原样提取)
├── build.gradle.kts / settings.gradle.kts / gradle.properties
└── gradle/wrapper/                 Gradle 8.13 wrapper
```

## 与反编译产物的差异(重建要点)

| 项 | 逆向源码 | 本工程 |
|---|---|---|
| 工程骨架 | 无(Gradle/AGP/清单) | 完整 Gradle 工程,AGP 8.11.1 + Gradle 8.13 + JDK 17 |
| 三方库 | 反编译的 kotlin-stdlib / commons-*/ xz 源码(1800+ 文件) | Maven 依赖:`commons-compress 1.26.2`、`commons-io 2.18.0`、`commons-lang3 3.17.0`、`xz 1.9` |
| `R` 资源类 | jadx 还原的 `R.java`(保留 ID 表) | 删除,由 AGP 根据 `res/` 自动生成 |
| `public.xml` / `drawables.xml` | jadx 副产品 | 删除 |

> 原 APK 构建链为 AGP 9.3.1;本工程改为成熟的 AGP 8.11.1 + Gradle 8.13(Java-only 应用,产物等价,
> Actions 首次构建成功率更高)。如需对齐原链,把 `build.gradle.kts` 中 AGP 升到 9.3.1、wrapper 换 Gradle 9.x 即可。

### 反编译伪码修复记录(jadx 产物无法直接编译的部分)

1. **`BootstrapInstaller.run()`**:`apt-get update` 的参数被 jadx 丢失(`r3, r2` 未定义变量)→ 重建为 `apt-get update`
2. **`BootstrapInstaller.download()`**:循环内混入伪异常路径(每轮写入后 `throw null` → 运行时必崩)→ 整体重写
3. **`BootstrapInstaller.fetchText()`**:循环内悬空 `throw th;`(未定义符号)→ 整体重写
4. **`BootstrapInstaller.extractTar()` / `extractProotFromDeb()`**:循环内悬空 `throw th;` / 自抑制 `addSuppressed(th)` 伪块 → 删除
5. **`FileManagerActivity.copyFile()`**:同上悬空 `throw th;` → 删除
6. **`FileManagerActivity.displayName()`**:catch 块后不可达语句(javac 报 unreachable)→ 重写
7. **`MainActivity.httpReady()`**:缺少 return 路径(missing return statement)→ 重写,取消 `throws Throwable`
8. **`MainActivity.waitForServerAndLoad()`**:`final boolean z` 在循环内赋值又被匿名类捕获(javac 报 "might be assigned in loop")→ 重写为语义等价版本
9. **`MobileUiInjector.readAsset()`**:循环内伪 `return "";`(会让注入永远为空)→ 删除
10. **`TerminalActivity.lambda$startShell$4()`**:循环内重复的"会话已结束"提示(伪码)→ 删除
11. **Kotlin lambda 反编译还原**:jadx 把 Kotlin lambda 还原成"匿名类引用外层实例的 `this.f$0` 字段",但字段未声明 → 全部改为直接引用外层实例 `<类>.this`(如 `MainActivity.this.lambda$xxx()`;其中 3 处目标方法属于 `DshBridge` 内部类,用 `DshBridge.this`),删除全部伪字段
12. **类型/语法杂项**:`Process` 二义性(android.os vs java.lang)→ 显式 `java.lang.Process`;`catch (NumberFormatException | Exception)` 子类重复 → 合并为 `catch (Exception)`;`extractLibsFromDeb` 裸 `HashMap` 迭代 → 泛型化;`Runnable.run() throws Throwable` 非法重写 → try/catch 包裹后抛出
13. **常量还原**:`IcTuple.NESTED_CLASS_FLAG`(丢弃的库常量,值 8)→ `8`;`KotlinVersion.MAX_COMPONENT_VALUE`(值 0xFF)→ `0xFF`,并删除两个错误 import

## 使用方法

### 方式一:推到自己仓库,用 Actions 编译(推荐)

1. 在 GitHub 新建一个仓库(如 `dsh-mobile`)
2. 把本目录内容推上去:
   ```bash
   cd dsh-mobile
   git init && git add . && git commit -m "dsh-mobile rebuilt from decompiled 1.0.19"
   git remote add origin <你的仓库地址>
   git push -u origin main
   ```
3. 仓库 → **Actions** 页面会自动跑 `Build APK` 工作流(也可手动 `workflow_dispatch`)
4. 构建完成在 Actions 运行页的 **Artifacts** 里下载 `dsh-mobile-apks`:
   - `app-debug.apk` — 调试签名,直接可安装
   - `app-release-unsigned.apk` — 本工程用调试签名代替(release 变体),直接可安装

### 方式二:本地 Android Studio 构建

用 Android Studio 打开 `dsh-mobile/` 目录(需 JDK 17 + Android SDK,会自动下载)。

```bash
./gradlew assembleDebug          # 构建 debug
./gradlew assembleRelease        # 构建 release(当前为调试签名)
```

APK 输出在 `app/build/outputs/apk/`。

### 方式三:一键安装本地构建工具(给想在本机编译的人)

不用手工下载 JDK/SDK,一条命令自动装好(自动识别 x86_64 / aarch64,走国内镜像):

```bash
bash tools/setup-tools.sh          # 装到 ~/tools
source ~/tools/env.sh              # 之后 java / javac / sdkmanager 直接可用
```

脚本会安装 OpenJDK 17 + Android cmdline-tools(含 platform 36)+ 预置 Gradle 8.13,幂等可重复执行。

> 反编译工具链(jadx)已独立成仓:[`Jack-kd/decompilation-tools`](https://github.com/Jack-kd/decompilation-tools)

## 分享给他人使用

三种方式,按对方需求选:

1. **Fork 本仓库 → Actions 编译(零安装,最推荐)**
   对方只需 GitHub 账号:进入本仓库页 → **Fork** → 自己仓库的 **Actions** 页面自动开始编译 →
   运行页 **Artifacts** 下载 `dsh-mobile-apks`(含 debug/release 两个可安装 APK)。全程不需要装任何工具。
   (注:仓库默认 private 时对方无法 Fork,需先公开或添加为 Collaborator。)

2. **本地一键脚本**:对方在自己的设备/电脑上执行
   `bash tools/setup-tools.sh && source ~/tools/env.sh && ./gradlew assembleDebug`
   即可在本机产出 APK(x86_64 / aarch64 均支持)。

3. **完整打包请在 GitHub Actions 上跑**:aapt2 无 aarch64 版,ARM 设备本地只能编译验证,不能打包。

## 签名说明

- 工程默认 **release 也用调试签名**(`app/build.gradle.kts` 中注释),保证 Actions 开箱即出**可安装**的 APK。
- 要出正式签名包:自己生成 keystore 后,把 `app/build.gradle.kts` 的 `signingConfigs` 换成你的配置,
  并把 keystore 密码放进仓库 Secrets(如 `KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD`),
  在 workflow 里解密后注入,避免把真 keystore 提交进仓库(已在 `.gitignore` 排除 `*.jks`)。
- 原 APK 的签名 keystore 无法恢复,重建包与官方包签名不同,**无法覆盖安装**在已装原版的设备上,需先卸载;全新安装无影响。

## 验证情况

- **Java 源码已通过本机 `javac`(JDK 17 + android.jar API 36)全量编译校验:0 error**(初始 83 个编译错误全部修复,见上表)。
- 资源/清单已按标准布局核对(`R` 引用全部能在 `res/` 中找到;`public.xml`/`drawables.xml` 等 jadx 副产品已剔除)。
- 本环境为 aarch64(无官方 Android build-tools aarch64 版),无法本地跑完整 AGP 构建;
  完整构建请在 x86_64 的 GitHub Actions 上执行(workflow 已就绪)。若首次构建有报错,把 Actions 日志贴来即可修。

## 许可声明

- 逆向产物原始 APK 未标注开源许可;若该 APK 源自 GPL 项目(如 DSHBox),公开发布重建包前请自行确认合规。
- 本工程仅用于学习与技术验证。