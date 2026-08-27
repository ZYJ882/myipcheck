# NetScope Android

NetScope 是一个以 **Kotlin、Jetpack Compose 与 Material 3** 编写的 Android 原生 IP 工具箱。它参考 [MyIP](https://github.com/jason5ng32/MyIP) 和 [IPCheck.ing](https://ipcheck.ing) 的公开信息架构与工具分类，但使用独立名称、独立 Android 界面和公开网络端点；本项目**不是官方客户端，也不隶属于 MyIP 或 IPCheck.ing**。

## 首批复刻能力

| 模块 | 当前 Android 原生实现 |
| --- | --- |
| 当前 IP 信息 | 查询公网 IPv4、可用 IPv6、国家/地区/城市、时区、ISP 与 ASN；支持复制 IPv4。 |
| 查询 IP | 输入任何 IPv4 或 IPv6，查询其位置、时区、ISP、ASN 与 IP 版本；查询结果自动进入本地历史。 |
| IP 历史 | 最多保存最近 30 条当前出口或手动查询记录；数据通过 Android Keystore AES-GCM 加密，仅保存在当前设备，支持清除。 |
| 连通性与端口 | 并行检测可编辑 HTTPS 目标，显示状态和端到端 HTTP 延迟；另可管理最多 12 个单主机单端口目标（端口 1–65535），配置加密本地保存。禁止路径、账户语法和端口范围，不提供扫描能力。 |
| 快速网络测量 | 用户手动触发后，对 Cloudflare Edge 执行 5 次轻量延迟采样和最多 **1 MB** 下载，展示中位延迟、抖动和下载吞吐；不上传数据、不自动运行。 |
| DNS 解析 | 支持 A、AAAA、TXT、MX、NS、CNAME；对域名执行 Android 系统解析，并使用 Cloudflare、Google Public DNS、Quad9 的 DoH 结果交叉核验，显示一致/有差异/无记录/未覆盖并可分享结果。地址差异不等于浏览器 DNS 泄漏。 |
| Whois | 连接公开 Whois 注册表查询域名或 IP 注册信息。 |
| 服务状态 | 将当前网络的单端口可达性与 GitHub、Cloudflare、OpenAI、Discord 官方公开状态摘要分开显示。官方状态不等于本机端到端可达性，也不是事故聚合。 |
| Android 网络环境 | 显示系统 VPN、Private DNS 模式和主机名、DNS 服务器、传输类型、系统互联网验证、按流量计费提示、接口及系统估算上下行带宽。系统估算不是实测带宽或浏览器指纹。 |
| 离线安全清单 | 提供 30 项设备、账户、网络、隐私和应急自评建议；进度经 Android Keystore AES-GCM 加密后仅存本机，可随时清除。它不会读取系统设置、扫描应用或判定设备安全。 |
| 显示主题 | 顶部显示设置可选跟随系统、始终浅色或始终深色；偏好加密存于本机，不会改变检测结论或发起网络请求。 |
| 增强纯净度诊断 | 使用公开行为风险证据生成独立主分，并将代理、VPN、Tor、IDC、ASN、覆盖度分开解释。它不是第三方专有分数或欺诈概率。 |
| 摘要分享 | 通过 Android 系统分享面板输出当前网络诊断的 Markdown 兼容纯文本或结构化 JSON 摘要；只在用户点击时执行，不上传、不生成公开链接，且不含授权 Key、加密 IP 历史或浏览器指纹。 |
| 网页专属工具 | 提供 BrowserLeaks、IPCheck WebRTC Leak、DNS Leak 和高级工具入口；网页结果不会静默写回 APP 或参与纯净度评分。 |

## 纯净度与授权数据源

主分采用 MyIPCheck 独立的 v3.1 公开网络出口风险信号模型，而不是 Ping0、MyIP、IPCheck.ing 或任何服务商的原始结论。主分只接受已观察到的公开行为证据；Tor、代理、VPN、中继、托管、IDC、ASN、CIDR 和地理属性只能用于网络透明度或上下文展示。没有独立、时间外真实标签校准前，分数不能解释为发生概率。

授权设置统一管理数据源。留空时，APP 仍默认使用 `ipify`、`ipapi.co`、`ipwho.is`、ProxyCheck、Tor Project 及上述公共 DNS 解析器。用户可选填 AbuseIPDB、ipapi.is、MaxMind GeoIP Insights 和 IPHub 的凭据来增加覆盖度；它们的官方接口需要有效授权，空 Key 时不会被调用。每个 Key 与 MaxMind Account ID 均可用小眼睛单独显示/隐藏，且全部用 Android Keystore AES-GCM 本地加密。

## 已知边界

WebRTC 候选地址、浏览器 Canvas/WebGL/JavaScript 指纹、完整递归 DNS Leak、PWA、键盘快捷键和网页 Persona Check 需要浏览器运行环境，不能由原生 Android API 等价复刻。APP 已按需支持受限的 Globalping ping 和 OONI 历史元数据查询，但 Globalping MTR、实时多国审查任务、可分享的过期链接、Earth Online 与官方服务事故聚合仍需要远端探针或后端服务，尚不在 APP 内伪装实现。

## 构建与安装

项目要求 Android Studio Ladybug 或更高版本、JDK 17 和 Android SDK 35。首次打开后让 IDE 完成 Gradle 同步，然后运行 `app` 配置；或执行：

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

调试 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

在已允许“安装未知来源应用”的 Android 设备上，将 APK 传到手机并安装即可。debug 包仅适合内测；仓库的 GitHub Release 工作流会使用维护者的固定签名密钥生成 release APK。

## 网络与隐私

网络请求会向相关的 IP、地理、风险、DNS、测速、Whois 或端口目标发送当前请求所需的最少信息。IP 历史、用户授权凭据、目标清单、安全清单进度和显示偏好仅加密保存在本机，云备份/设备迁移均被禁用。APP 不读取账号、Cookie、浏览历史、通讯录、位置或浏览器指纹。第三方服务可能按各自隐私政策记录请求，返回结果也可能不准确、过时或不可用。

## 参考来源

[1] [jason5ng32/MyIP](https://github.com/jason5ng32/MyIP)

[2] [IPCheck.ing](https://ipcheck.ing/)

[3] [Cloudflare Speedtest](https://github.com/cloudflare/speedtest)

[4] [Globalping API](https://globalping.io/docs/api.globalping.io)

[5] [OONI data/API documentation](https://docs.ooni.org/data)
