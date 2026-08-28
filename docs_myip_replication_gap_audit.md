# MyIPCheck 对照 MyIP / ipcheck.ing 复刻差异审计

**审计对象**：`ZYJ882/myipcheck` Android 原生应用，当前源码基线为 v1.0.22 发布批次。

**对照对象**：[jason5ng32/MyIP][1] 与 [ipcheck.ing][2] 的公开功能入口。

**审计性质**：本文按公开项目资料、公开接口文档及 MyIPCheck 当前源码进行能力对照。状态仅表示 Android 端的实际实现范围：**已实现**、**部分实现**、**未实现**或**架构不等价**。它不把外部网页入口、远端历史数据或第三方状态 API 误称为本机原生检测，也不将功能项数量视为代码覆盖率。

## 总结结论

MyIPCheck 现已覆盖一个更完整的**移动端网络诊断子集**：任意 IP 查询与加密历史、可编辑 HTTPS 连通性、受限的单主机单端口探测、A/AAAA/TXT/MX/NS/CNAME DNS 解析与多解析器差异摘要、Whois、ASN 概览与 RIS 邻居、MAC/OUI 查询、轻量 Cloudflare 测速、按需 Globalping 与 OONI 历史元数据，以及 GitHub、Cloudflare、OpenAI、Discord 的官方状态摘要。

它仍不是 MyIP 或 ipcheck.ing 的完整客户端。最重要的缺口是浏览器专属能力、全球后端服务和大型内容系统：**WebRTC/SDP 泄漏、Canvas/WebGL/浏览器指纹、真正递归 DNS Leak 实验、Globalping MTR、代理规则测试、完整审查测量、可过期在线报告、Curl API、Earth Online、Persona/Invisibility 深度页面与完整安全清单**。

> Android 展示的 VPN、Private DNS、网络传输、系统 DNS、网络验证、接口和系统带宽估算仅描述 Android 当前网络能力；它们不是 WebRTC、JavaScript 指纹或浏览器 DNS 泄漏结论。DNS 解析器结果不同也可能源于 CDN、地域、缓存或策略，**不构成 DNS 泄漏证据**。

## 逐项差异矩阵

| 目标能力 | MyIP / ipcheck.ing 公开行为 | MyIPCheck v1.0.22 状态 | 差异与判断 | 优先级 |
|---|---|---|---|---:|
| 当前与任意 IP 信息 | 多探针 IPv4/IPv6、地理、ASN、组织、时区、任意 IP 查询 | **已实现基础版** | 支持当前 IP 与输入 IPv4/IPv6 查询，提供公开默认源与可选授权源；字段丰富度和服务端源编排仍低于目标站点。 | P0 |
| IP 历史 | 浏览器本地历史及筛选 | **已实现基础版** | 查询结果加密本地保存，最多 30 条并可清除；暂未实现按国家、类型等筛选。 | P1 |
| 网络连通性 | 约 60 个可选网站、多轮最小延迟、导入和分类 | **已实现基础版** | HTTPS 目标可添加、删除、重置，最多 12 项并加密保存；新增生产力 HTTPS 预设导入，但仍是单轮手动测量，无上游完整大清单分类。 | P1 |
| 端口状态 / 探测 | 网络工具箱中的目标服务可用性能力 | **已实现，受限设计** | 用户管理最多 12 个**单主机单端口**目标（1–65535）；禁止路径、账户语法和端口范围，不能用作端口扫描器。 | P1 |
| WebRTC 泄漏 | STUN、候选地址、NAT、SDP 及浏览器网络信息 | **架构不等价** | 原生 Android 不能等价复现浏览器 WebRTC/SDP 环境；仅可提供外部网页入口或单独的 WebView 方案。 | P1（网页）/P2（原生） |
| DNS Leak Test | 随机域名、递归 DNS endpoint、地理/ISP 归因 | **部分实现** | 系统解析及 DoH 交叉的 A/AAAA/TXT/MX/NS/CNAME 查询、共识/差异摘要和文本分享已实现；不执行随机域名递归泄漏实验，也不做 DNS 出口地理归因。 | P1 |
| DNS Resolution | 多解析器、国家分组、深度模式含 ECS/DNSSEC | **部分实现** | 已交叉系统、Cloudflare、Google Public DNS、Quad9、DNS.SB，并显示结果差异；尚无国家分组、ECS/DNSSEC 校验或全部递归解析器枚举。 | P1 |
| 浏览器指纹 / Browser Information | Canvas、WebGL、JS 特征及浏览器环境 | **架构不等价** | APK 没有同一浏览器上下文；不应将设备信息伪装为浏览器指纹检测。 | P2 |
| Invisibility Test | 独立验证代理/VPN 使用状态 | **部分实现** | 透明度诊断可并列显示代理、VPN、Tor、IDC/ASN 等公开信号；没有独立的目标站点语义或专用报告页面。 | P2 |
| Security Checklist | 大型安全清单、分类和本地进度 | **已实现基础版** | 已提供 30 项按领域分组的离线自评建议、搜索和加密本地进度；未复刻上游全部 258 项，也不读取系统设置、扫描应用或给出合规结论。 | P2 |
| Speed Test | Edge 下载、上传、延迟、抖动、可选包大小 | **部分实现** | 已测 Cloudflare 轻量延迟、中位延迟、抖动及最多 1 MB 下载吞吐；无上传测速、长时多档位测试或运营商级对比。 | P1 |
| Global Latency Test | Globalping 多国家/大洲探针 Ping | **已实现，受限调用** | 用户点击后最多从美国、德国、新加坡各请求 1 个探针、3 个 ping 包；遵守公开 API 配额，不代表本机延迟。 | P1 |
| MTR Test | Globalping / 全球探针路径追踪 | **已实现，受限调用** | 用户点击后最多从美国、德国、新加坡各请求 1 个探针，展示最多 64 跳的结构化主机/IP/ASN/丢包/延迟；结果是远端探针路径观测，不代表本机路由。 | P1 |
| Proxy Rule Test | 验证代理软件规则分流是否符合预期 | **未实现** | 无规则输入、目标比对或分流报告。 | P2 |
| Censorship Check | 多国网站可达性及阻断方式 | **部分实现** | 可查询 OONI 最多 5 条公开 Web Connectivity 历史元数据；不等于本机实时审查检测，也无多国主动任务。 | P1 |
| Whois Search | IP/域名 Whois | **已实现基础版** | 已有 IANA/注册表链式查询和结果展示；仍缺更丰富的注册局字段整合与历史记录。 | P1 |
| MAC Lookup | MAC/OUI 厂商查询和详情 | **已实现基础版** | 支持手动 MAC 查询 MACVendors 公开 OUI 数据；不读取设备 MAC，且对本地管理 MAC 不作可靠 OUI 厂商归因。 | P2 |
| ASN Info | ASN 详情 | **已实现基础版** | 支持 RIPEstat ASN 概览和当前 IP ASN 信息；可继续丰富前缀、地理、注册信息和历史。 | P1 |
| ASN 历史与拓扑 | 前缀历史、到 Tier 1 的上游路径 | **部分实现** | 已显示 RIS AS-path 左右邻居；它们是观测路径位置，不能自动解释为商业上下游，也没有历史时间线或图形。 | P2 |
| Service Status | 官方状态、实时可用性、近期事故 | **部分实现** | 显示 GitHub、Cloudflare、OpenAI、Discord 的公开官方状态摘要，并与本机端口连通性分开；未做跨服务事故聚合与长期事件历史。 | P1 |
| Shareable Reports | 过期链接、Markdown、JSON | **部分实现** | 支持 Android 系统分享 Markdown 兼容文本、DNS 文本结果及结构化 JSON 摘要；没有服务器托管、过期 token 或公开链接。 | P1 |
| Curl API | `curl` 获取 IP | **未实现** | Android 客户端并未部署对外兼容 API。 | P2 |
| Earth Online | 全球互联网中断广播 | **未实现** | 无公共 outage feed 聚合、地图或事件面板。 | P2 |
| Dark Mode | 系统跟随和手动切换 | **已实现基础版** | 可选择跟随系统、始终浅色或始终深色，界面调色板随选择切换；尚无 Material You 动态色、定时切换或每页独立外观设置。 | P2 |
| PWA / Chrome App | 可安装 Web/PWA 体验 | **架构不适用** | Android APK 不等同 PWA；如需复刻，应另建 Web 前端。 | P2（另项目） |
| Keyboard Shortcuts | 工具页快捷键和帮助 | **架构不适用** | 以触控移动端为主，没有桌面快捷键体系。 | P2 |
| 多语言 | 多语言 locale pack | **未实现** | 当前主要为简体中文，尚无 Android locale 资源体系。 | P1 |
| AI/IPilot | 首页 IP 问答和解释助手 | **未实现** | 无 LLM 助手，不应把固定规则说明称作 AI 问答。 | P2 |
| In-depth DNS Leak | ECS、DNSSEC、递归解析器全量分析 | **未实现** | 多 DoH 结果差异是解析对比，不能替代深度 DNS Leak 测试。 | P2 |
| In-depth Persona Check | 目标地区视角与网站可见信息对比 | **未实现** | 需要浏览器会话、远端视角或后端协作，原生本机字段不足以等价复刻。 | P2 |
| 授权与风险数据源 | 服务器侧配置 MaxMind 等数据 | **已实现本地授权子集** | AbuseIPDB、ipapi.is、MaxMind Insights、IPHub 可在授权设置中启用，凭据经 Android Keystore AES-GCM 本地加密保存；空 Key 时回退公共默认源。 | P0 |

## v1.0.21 已实现能力及其边界

当前单页原型已经包含 IP/IPv6、风险信号与透明度、HTTPS 连通性、Android 网络环境、轻量测速、DNS、Whois、端口探测、ASN、MAC、Globalping、OONI、服务状态、加密历史、离线安全清单、主题选择以及 Markdown/JSON 本地导出。所有主动网络请求由用户操作触发；不设后台持续轮询，也不申请额外的敏感权限。

| 模块 | 当前可靠表述 | 不应作出的表述 |
|---|---|---|
| 风险评分 | 是基于公开滥用行为证据、时效和覆盖度的可解释 0–100 信号指数。 | 不是 Ping0、账户风险、欺诈概率、信誉保证或绝对安全结论。 |
| 单端口探测 | 是到用户指定的一台主机、一个端口的 TCP 可达性检查。 | 不是端口范围扫描、资产发现或安全漏洞扫描。 |
| DNS 共识 | 是多个可用解析路径的答案对比和差异提示。 | 不是递归 DNS 泄漏、ECS、DNSSEC 或运营商劫持的确定性判断。 |
| 官方状态 | 是供应商官方公开状态端点返回的摘要。 | 不等于手机到其服务的端到端可达性，也不能替代事故诊断。 |
| 网络环境 | 是 Android `NetworkCapabilities` 和链路属性可见的网络状态。 | 不是实测速率、WebRTC、Canvas/WebGL、JavaScript 或网页指纹检测。 |
| 安全清单 | 是用户自行确认的离线设备与账户维护提醒，进度仅加密保存在本机。 | 不是系统设置扫描、恶意软件检测、合规证明或安全保证。 |
| 本地导出 | 是由用户点击触发的 Markdown/JSON 快照分享，且不含授权凭据和 IP 历史。 | 不是服务器报告托管、在线公开链接或长期证据存档。 |

## 下一批的合理优先级

第一优先级应当是**多语言资源体系**、更精细的本地导出筛选以及安全清单的可访问性与搜索体验。这些能力不需要把浏览器字段或第三方后端冒充为本机结果。第二优先级可以在严格配额和明确远端语义的前提下扩展 Globalping MTR，以及改善 OONI 查询的筛选和结果展示。任何真正的 WebRTC、浏览器指纹、Persona 和深度 DNS Leak 功能均应采用独立 Web companion 或受控后端，并在界面清晰标识隐私、数据来源和非原生范围。

## 架构建议

目前大部分 Android UI、网络请求、数据模型和评分逻辑仍集中于 `MainActivity.kt`，适合快速原型而不利于继续扩展。后续宜拆为 `feature/ipinfo`、`feature/connectivity`、`feature/dns`、`feature/advanced`、`feature/report`、`data/provider` 与 `domain/scoring`；每个 Provider 都应返回来源、时间、覆盖度、配额和错误状态的标准模型，UI 不应直接解析 JSON。

Globalping、OONI、官方状态和未来报告分享必须持续尊重公开 API 的使用限制与来源边界。对于 MaxMind、IPHub 等授权源，必须维持“空 Key 只走公共默认源”的行为，不能把密钥放入 URL、日志或 Git 历史。对于后续需后端的功能，应先建立最小、可审计的服务端权限和数据保留策略，再向 APK 提供接口。

## 参考资料

[1] [jason5ng32/MyIP GitHub repository](https://github.com/jason5ng32/MyIP)

[2] [IPCheck.ing live demonstration](https://ipcheck.ing/)

[3] [MyIP repository API tree](https://github.com/jason5ng32/MyIP/tree/main/api)

[4] [MyIP repository frontend tools](https://github.com/jason5ng32/MyIP/tree/main/frontend/components/advanced-tools)

[5] [Globalping documentation](https://globalping.io/docs)

[6] [OONI Explorer](https://explorer.ooni.org/)

[7] [MaxMind GeoIP Web Services](https://dev.maxmind.com/geoip/docs/web-services/)

[8] [IPHub API documentation](https://iphub.info/api)

[9] [Android NetworkCapabilities reference](https://developer.android.com/reference/android/net/NetworkCapabilities)
