# MyIPCheck Android

> **一个根据开源 [MyIP](https://github.com/jason5ng32/MyIP) 的 IP 工具箱定位，并参考 [IPCheck.ing](https://ipcheck.ing) 公开信息架构而构建的 Android 原生 APP。**

**MyIPCheck Android** 面向手机端提供当前公网 IP、网络连通性、Android 网络隐私、DNS、Whois、服务状态与公开出口风险信号诊断。项目使用 **Kotlin、Jetpack Compose 与 Material 3** 独立实现，强调移动端可读性、按需网络请求与可解释的结果展示。

## 项目缘起与上游关系

开源项目 [MyIP](https://github.com/jason5ng32/MyIP) 是一个功能丰富的 IP 工具箱，其公开能力包括 IP 信息、可用性检查、WebRTC、DNS 泄漏、测速、DNS Resolver、Whois 和浏览器指纹等；[IPCheck.ing](https://ipcheck.ing) 是其公开演示站点。[1] [2] 本项目依据这些公开的产品方向和信息组织方式，结合 Android 手机上的实际使用场景，开发为一个可安装的原生客户端。

| 项目 | 定位与关系 |
| --- | --- |
| [MyIP](https://github.com/jason5ng32/MyIP) | 上游开源 IP 工具箱，提供本项目参考的功能分类、网络诊断思路与公开产品方向。 |
| [IPCheck.ing](https://ipcheck.ing) | MyIP 的公开演示站点，本项目参考其信息层级和工具入口组织。 |
| MyIPCheck Android | 本仓库的独立 Android 原生实现；使用 Kotlin / Compose 编写，不是 MyIP 或 IPCheck.ing 的官方客户端。 |

> **重要声明：** MyIPCheck Android 不代表上游项目作者，也不使用或声称使用 MyIP、IPCheck.ing、Ping0 或其他第三方服务的私有服务器、专有风控模型、人工标注数据或用户数据。应用名称、界面、图标、网络实现与 Android 交互均独立处理。

## 核心能力

| 模块 | Android 原生实现内容 |
| --- | --- |
| 公网 IP 信息 | 查询 IPv4、可用时的 IPv6、国家 / 地区 / 城市、时区、ISP 与 ASN，并支持复制 IPv4。 |
| 网络连通性 | 检测 Google、GitHub、Cloudflare、ChatGPT、YouTube 与 Wikipedia 的可达性和延迟。 |
| 增强纯净度诊断 | 以公开滥用行为证据生成连续主分，并把 Proxy / VPN / Tor / 中继、托管 / IDC / ASN 和证据覆盖度独立展示；每一分均可回放来源、字段与时效。 |
| Android 网络隐私 | 展示 Android VPN 状态、Private DNS 模式与系统 DNS 服务器。 |
| 原生 DNS 与 Whois | 使用 Android 系统解析器进行 DNS 查询，并通过公开注册表查询域名或 IP 的 Whois 信息。 |
| 原生服务状态 | 从当前网络探测常用 HTTPS 服务的 443 端口连通性和连接耗时。 |
| 轻量网络测量 | 对 Cloudflare 执行单次 HTTP 延迟测量，不自动发起大流量上传或下载。 |
| 设备环境 | 展示 Android 版本、语言、应用标识等本机环境信息。 |
| 浏览器备用入口 | 仅在 WebRTC、JavaScript 指纹或真实多地区探针等依赖浏览器环境时，才打开 IPCheck.ing。 |

## 为什么是 Android 原生 APP

MyIP 与 IPCheck.ing 包含一些典型的浏览器能力，例如 WebRTC 候选地址、JavaScript 指纹和多地区外部探针。Android 原生应用不具备完全等价的浏览器运行上下文，因此 MyIPCheck 优先在 APP 内完成 DNS、Whois、端口可达性、VPN、Private DNS 与 DNS 服务器诊断；对于确实依赖浏览器或全球探针网络的功能，只保留明确标注的浏览器备用入口。

这种边界设计避免了把“原生环境无法检测”误写成“安全”或“存在泄漏”。每项结果均以当前网络与当前检测时刻为准，不替代专业安全审计、账号风控或合规判断。

## 透明纯净度诊断

纯净度诊断现为 MyIPCheck 独立设计的**公开网络出口风险信号指数**。它不是任何第三方的原始评分，也不是个人、账号、支付或交易的欺诈概率。v3.1 将输出拆为：**公开滥用风险主分、网络透明度、网络上下文和证据覆盖度**。主分只接受可用的公开行为证据；Tor、代理、VPN、中继、IDC、托管、ASN、CIDR、地理与出口差异均被单列解释，绝不凭网络属性推断历史恶意。

主分中的每一分均由来源、字段、来源质量先验、计数饱和、事件时间衰减和非线性家族组合计算。APP 同时显示**证据覆盖度**：接口未覆盖、Key 未配置、429、超时、字段缺失或格式错误均不扣风险，但会降低覆盖度，并明确显示为“未覆盖”而不是“未检出”。未经外部、时间外真实标签校准，任何分数都不得解释为概率。

详细的公式、上限、去重方法、覆盖度定义、示例情景和引用见：

- [`purity_scoring_model_v3_1.md`](purity_scoring_model_v3_1.md)
- [`docs_maxmind_iphub_integration.md`](docs_maxmind_iphub_integration.md)
- [`docs_external_sites_assessment.md`](docs_external_sites_assessment.md)
- [`IP纯净度评分规则核验与v3_1建议.md`](IP纯净度评分规则核验与v3_1建议.md)

### 授权数据源 Key

点击 APP 顶部**地球图标右侧的钥匙图标**可统一管理服务商。**不填写任何授权时，APP 会自动使用 ipify、ipapi.co、ipwho.is、ProxyCheck 与 Tor Project 的默认公共检测源。** 如拥有 AbuseIPDB、ipapi.is、MaxMind GeoIP Insights 或 IPHub 的凭据，可在同一界面选填以增加可用证据；每个凭据输入框旁均有小眼睛，可在本机即时显示或隐藏。所有 Key、MaxMind Account ID 和自定义 HTTPS 请求地址均使用 Android Keystore 的 AES-GCM 密钥加密后存储在当前设备；系统云备份与设备迁移已禁用。

| 配置项 | 当前行为 |
| --- | --- |
| 默认公共检测 | 无需配置，自动调用 ipify、ipapi.co、ipwho.is、ProxyCheck 与 Tor Project；所有可选授权服务留空时仍会工作。 |
| AbuseIPDB API Key | 选填；保存后用于查询 AbuseIPDB APIv2 Check；置信分、报告量、独立报告者与最近报告构成公开滥用证据候选。 |
| ipapi.is API Key | 选填；保存后通过官方 JSON POST 查询安全属性；`is_abuser` 与 `is_crawler` 可进入相应行为家族，匿名化与数据中心只单列。 |
| MaxMind Account ID + License Key | 选填且必须成对；以 HTTPS Basic Auth 调用 GeoIP Insights；匿名化与网络字段只进入透明度、上下文和覆盖度。 |
| IPHub API Key | 选填；以 `X-Key` 调用 v2.2；`block==1` 和 `proxyType` 只作为透明度/上下文，`block==2` 仅提示低置信。 |
| 自定义 HTTPS 地址与 Key | 必须成对保存；当前版本仅本地加密保存，不会自动请求、不发送当前 IP，也不参与评分。 |

具体操作与隐私边界见 [`API-Key-配置指南.md`](API-Key-配置指南.md)。

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

## 使用与开发

### 本地构建

使用 Android Studio 打开 [`android`](android) 目录，并准备 JDK 17 与 Android SDK 35；也可在终端执行：

```bash
cd android
./gradlew :app:assembleDebug
```

调试 APK 会生成在 `android/app/build/outputs/apk/debug/app-debug.apk`。详细 Android 模块说明见 [`android/README.md`](android/README.md)。

## 自动化开发流程

仓库保留 [Android 自动化工作流](.github/workflows/android-release.yml) 供维护者处理源码压缩包。压缩包可使用 ZIP、TAR、TAR.GZ 或 TGZ，内部需包含可构建的 Gradle 工程，例如 `settings.gradle.kts`、`build.gradle.kts` 和 `app/` 目录；详细约定见 [`uploads/README.md`](uploads/README.md)。

## 网络与隐私

应用使用 `api.ipify.org` 获取公网 IP，并使用 `ipapi.co` 与 `ipwho.is` 查询 IP 地理属性；增强诊断按需访问 `proxycheck.io` 与 Tor Project。只有用户自行配置对应 Key 后，才会调用 AbuseIPDB APIv2 Check、ipapi.is 官方 JSON POST、MaxMind GeoIP Insights 与 IPHub v2.2。`myip.edgeone.ai` 在本次核验中无法解析，NSTool 未公开可审计 IP API，二者均不会被自动请求。BrowserLeaks 仅作为浏览器专属的外部自检入口。自定义 HTTPS 地址与 Key 当前不会被发送给任何端点。

应用不会读取账号、Cookie、浏览记录、设备指纹、通讯录或位置权限；也不会在本地持久化保存 IP 历史或检测报告。网络服务与公开注册表仍可能按照各自隐私政策处理请求日志，使用前请自行确认服务条款、账户配额与隐私政策。

## 致谢与参考

感谢 [MyIP](https://github.com/jason5ng32/MyIP) 项目及其作者对开源 IP 工具箱生态的贡献，也感谢 [IPCheck.ing](https://ipcheck.ing) 提供公开演示与产品参考。MyIPCheck Android 以独立 Android 原生实现的方式，将其中适合移动端的公开工具方向带到手机设备上。[1] [2]

[1] [jason5ng32/MyIP — A Better IP Toolbox](https://github.com/jason5ng32/MyIP)

[2] [IPCheck.ing](https://ipcheck.ing)
