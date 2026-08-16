# MyIPCheck Android

**MyIPCheck Android** 是一个基于 [MyIP](https://github.com/jason5ng32/MyIP) 公开功能架构实现的 Android 原生 IP 工具箱。项目将 IPCheck.ing 的核心信息层级和网络检测思路移植到 Android 手机端，使用 **Kotlin、Jetpack Compose 和 Material 3** 构建，重点提供公网 IP 查询、IP 地理信息、网络连通性检测、Android 隐私诊断和轻量网络延迟测量。

> 本项目是独立的第三方 Android 实现，不是 MyIP 或 IPCheck.ing 的官方客户端，也不代表原项目作者。项目在界面、应用名称、启动图标和客户端实现上均进行了独立处理。

## 项目特点

MyIPCheck Android 采用适合移动端的单列卡片布局，将常用网络信息集中在首页。应用启动后会自动加载公网 IP 和地理信息，并并行测试常用服务的 HTTP 可达性；用户也可以单独重试某一个服务或刷新全部结果。

| 模块 | 功能说明 |
| --- | --- |
| IP 信息 | 查询公网 IPv4、可用时的 IPv6、国家/地区/城市、时区、ISP 与 ASN，并支持复制 IPv4。 |
| 网络连通性 | 检测 Google、GitHub、Cloudflare、ChatGPT、YouTube 和 Wikipedia，显示可达状态与延迟。 |
| Android 隐私诊断 | 展示当前 VPN 状态、Private DNS 模式和系统 DNS 服务器。 |
| 轻量网络测量 | 对 Cloudflare 执行一次轻量 HTTP 延迟测量，不自动进行大流量上传或下载。 |
| 扩展工具 | 通过系统浏览器打开 IPCheck.ing 的全球延迟、DNS 解析、Whois、浏览器信息和服务状态工具。 |

## 为什么采用 Android 原生实现

MyIP 原项目和 IPCheck.ing 官网包含浏览器环境相关能力，例如 WebRTC 候选地址检测和 JavaScript 浏览器指纹。Android 原生应用并不具备完全等价的浏览器运行上下文，因此本项目不会伪造 WebRTC 泄漏或浏览器指纹结论，而是展示 Android 系统能够可靠提供的 VPN、Private DNS 与 DNS 信息。这种处理方式能够避免将“无法检测”误报成“安全”或“存在泄漏”。

## 技术栈

| 项目 | 版本或方案 |
| --- | --- |
| 编程语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material 3 |
| 构建系统 | Gradle Wrapper 8.10.2 |
| Android Gradle Plugin | 8.6.1 |
| Compile SDK | Android API 35 |
| 最低 Android 版本 | Android 8.0 / API 26 |
| 网络实现 | `HttpURLConnection` + Kotlin Coroutines |
| 应用包名 | `ing.ipcheck.netscope` |

## 目录结构

```text
android/
├── app/
│   ├── src/main/java/ing/ipcheck/netscope/MainActivity.kt
│   └── src/main/res/
├── gradle/
├── releases/NetScope-debug.apk
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

## 快速使用

如果只想体验应用，可以直接下载 [`android/releases/NetScope-debug.apk`](android/releases/NetScope-debug.apk)，传输到 Android 设备后安装。该文件是用于测试和演示的 debug APK，不适合直接作为应用商店发布包。

如果需要进行二次开发，请使用 Android Studio 打开 [`android`](android) 目录，并确保本机安装 JDK 17 和 Android SDK 35。也可以在终端执行：

```bash
cd android
./gradlew :app:assembleDebug
```

构建产物会生成在 `android/app/build/outputs/apk/debug/app-debug.apk`。正式发布前，请使用自己的 release 签名密钥，并根据发布渠道配置版本号、隐私政策和应用商店元数据。

## 网络端点与隐私说明

应用使用 `api.ipify.org` 获取公网 IP，并使用 `ipapi.co` 查询 IP 地理信息；网络连通性模块会对页面中列出的服务发起少量 HTTPS 请求。应用不会在本地持久化保存 IP 历史或检测报告，但第三方服务可能按照其自身隐私政策处理请求日志。用户应在正式部署或公开发布前，对接口可用性、服务条款和隐私政策进行复核。

## 构建验证

当前工程已验证以下命令可以成功执行：

```bash
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

当前 debug APK 的包名为 `ing.ipcheck.netscope`，版本为 `1.0.0`，最低系统版本为 API 26，目标版本为 API 35。Lint 检查能够成功完成；其中保留了少量依赖版本更新建议，因为直接升级到最新依赖会要求更高版本的 Android Gradle Plugin 和 Compile SDK，与当前可复现的 Android API 35 构建环境不兼容。

## 自动构建与发布

仓库已经加入 `.github/workflows/android-release.yml`。以后只需把完整 Android 源码压缩包上传到 [`uploads/`](uploads/) 目录并提交到 `main` 分支，GitHub Actions 就会自动执行以下流程：选择最新源码压缩包、解压并规范化到 `android/`、构建 release APK、校验 APK 签名、递增 `versionCode` 和 `versionName`，最后创建一个新的 GitHub Release 并上传 APK。

源码压缩包可以是 ZIP、TAR、TAR.GZ 或 TGZ。压缩包内部应包含完整 Gradle 工程，也就是能够看到 `settings.gradle.kts`、`build.gradle.kts` 和 `app/` 目录；允许外面再包一层文件夹。上传文件的详细约定见 [`uploads/README.md`](uploads/README.md)。也可以在 Actions 页面手动运行 `Build and Release Android APK`，并在 `archive_path` 中填写指定压缩包路径。

### 一次性配置签名 Secrets

为了让新版本覆盖当前已经上传的 `NetScope-debug.apk`，后续 release APK 必须使用同一份 Android 签名证书。当前 APK 的签名证书 SHA-256 指纹为 `A2:1E:DD:CD:75:53:D2:9F:85:2F:CA:FB:CE:C0:4C:E7:FC:41:B7:3E:26:81:B8:97:BC:A9`。该版本使用 Android debug keystore 构建，因此需要把**同一份 keystore 文件**保存为 GitHub Actions Secret，不能重新生成一份新的密钥。

拥有仓库 Actions Secrets 写入权限的电脑上，可以执行以下命令。命令只会把密钥保存到 GitHub Secrets，不会把 keystore 提交进仓库：

```bash
base64 -w0 ~/.android/debug.keystore | gh secret set ANDROID_KEYSTORE_BASE64 --repo ZYJ882/myipcheck
gh secret set ANDROID_KEYSTORE_PASSWORD --body android --repo ZYJ882/myipcheck
gh secret set ANDROID_KEY_ALIAS --body androiddebugkey --repo ZYJ882/myipcheck
gh secret set ANDROID_KEY_PASSWORD --body android --repo ZYJ882/myipcheck
```

如果当前电脑没有这份原始 debug keystore，不能通过新生成的密钥实现覆盖安装；此时必须先卸载旧 APK，或者找回旧 keystore 后再配置 Secrets。GitHub Actions 工作流本身已经准备好读取以上四个 Secrets，但出于安全原因，仓库不会保存签名私钥。新手可以直接按照 [`GitHub-Secrets-操作指南.md`](GitHub-Secrets-操作指南.md) 操作。

### 版本与覆盖安装

自动发布版本使用 `versionCode = 1000 + GitHub Actions run number`，版本名称采用 `1.0.<run number>`，因此每次成功构建都会高于之前的版本。只要 `applicationId` 保持为 `ing.ipcheck.netscope`、签名证书保持一致且 `versionCode` 递增，Android 就可以将新 APK 作为升级包覆盖安装，而不需要先卸载旧版本。

## 致谢与参考资料

本项目的功能分类和信息架构参考了 MyIP 开源项目及 IPCheck.ing 官网，感谢原项目作者对网络诊断工具的开源贡献。

[1]: https://github.com/jason5ng32/MyIP "MyIP 开源项目"
[2]: https://ipcheck.ing/ "IPCheck.ing 官方网站"
[3]: https://ipcheck.ing/tools/ "IPCheck.ing 工具列表"
