# NetScope Android

NetScope 是一个使用 **Kotlin + Jetpack Compose + Material 3** 编写的 Android 原生 IP 工具箱。它以 MyIP / IPCheck.ing 的公开信息架构为视觉与功能参考，但使用独立名称、独立界面实现和公开网络端点；本项目**不是官方客户端，也不隶属于 MyIP 或 IPCheck.ing**。

## 已实现能力

| 模块 | 功能 |
| --- | --- |
| IP 信息 | 查询公网 IPv4、IPv6（如可用）、国家/地区/城市、时区、ISP 与 ASN；可一键复制 IPv4。 |
| 连通性 | 并行检测 Google、GitHub、Cloudflare、ChatGPT、YouTube、Wikipedia，显示状态和端到端 HTTP 延迟；支持单项重试和全部刷新。 |
| Android 隐私诊断 | 显示系统 VPN 连接、Private DNS 工作模式与当前系统 DNS。应用明确说明 WebRTC 浏览器测试无法被原生 API 等价替代。 |
| 透明纯净度诊断 | 多源出口和国家一致性、双栈位置、公开 ASN / 组织属性及 Android 网络状态的 0–100 可解释评分。 |
| 轻量延迟 | 对 Cloudflare 发起一次轻量 HTTP 请求；不会触发大流量上传或下载。 |
| DNS 解析 | 通过 Android 系统解析器直接查询域名的 IPv4 / IPv6 地址。 |
| Whois 查询 | 连接公开 Whois 注册表查询域名或 IP 注册信息。 |
| 服务状态 | 从当前网络探测常用 HTTPS 服务的 443 端口可达性与连接耗时。 |
| 设备环境 | 显示 Android 版本、语言与应用标识等原生环境信息。 |
| 浏览器备用入口 | 仅在需要 WebRTC、JavaScript 指纹或真实多地区探针时打开 IPCheck.ing。 |

## 纯净度诊断边界

本模块参考 Ping0 公开的风险、IP 类型、原生性与共享稳定性等概念，但不使用或伪称其专有人工 IP 段标注、恶意行为信誉、共享人数、BGP / ASN / 企业历史或注册地历史。分数仅反映本次检测时的公开出口一致性与网络属性，未知数据不扣分，不可作为账号、支付、广告或合规决策的唯一依据。具体规则见仓库根目录 [`purity_module_spec.md`](../purity_module_spec.md)。

## 界面设计

设计保留原站“冷白页面 + 白色信息卡片 + 语义状态色”的信息层级，但针对手机纵向单列使用做了原生优化。蓝色代表信息/等待，绿色代表可达，琥珀色代表提示，红色代表失败；所有状态均有文本和图标，避免仅依赖颜色。

## 构建与安装

项目要求 Android Studio Ladybug 或更高版本，JDK 17 和 Android SDK 35。首次打开后让 IDE 完成 Gradle 同步，然后运行 `app` 配置；或在终端执行：

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

生成的 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

在已允许“安装未知来源应用”的 Android 设备上，将该 APK 传到手机并点击安装即可。该 debug 包只适合内测与演示，发布到应用商店前应使用自己的签名密钥生成 release 包。

## 网络与隐私

应用访问 `api.ipify.org` 获取公网 IP，访问 `ipapi.co` 查询 IP 地理信息。连通性与服务状态检测会对所列服务发起少量 HTTPS 或 TCP 连接，DNS 查询使用系统解析器，Whois 查询连接公开注册表。应用不收集、不上传或持久化保存用户 IP 与检测历史；数据仅保留在当前应用内存中。由于 IP 地理信息与 Whois 信息来自第三方公共服务，信息可能不准确或不可用。

## 参考来源

功能分类和信息架构参考 MyIP 的公开开源项目与 IPCheck.ing 官网。[1] [2]

[1]: https://github.com/jason5ng32/MyIP "jason5ng32/MyIP"
[2]: https://ipcheck.ing/ "IPCheck.ing"
