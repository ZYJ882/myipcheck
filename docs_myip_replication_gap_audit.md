# MyIPCheck 对照 MyIP / ipcheck.ing 复刻差异审计

**审计对象**：`ZYJ882/myipcheck` Android 原生应用

**对照对象**：[jason5ng32/MyIP](https://github.com/jason5ng32/MyIP) 与 [ipcheck.ing](https://ipcheck.ing/)

**审计性质**：基于目标项目公开 README、仓库目录/API 文件名、ipcheck.ing 首页公开入口，以及 MyIPCheck 当前 Android 源码与 README 的静态对照。结论区分“已实现”“部分实现”“尚未实现”和“架构上不等价”，不把 Android 外部网页入口误判成原生复刻。

## 总结结论

MyIPCheck 已经复刻了一个**移动端核心子集**：当前 IP 与 IPv6、基础地理和 ASN 展示、连通性、DNS/Whois、端口状态、Android VPN/Private DNS、Cloudflare 轻量延迟、公开风险信号诊断，以及部分浏览器外部入口。它目前还不是 MyIP/ipcheck.ing 的完整功能复刻。

按功能项粗略统计，当前状态约为：核心信息与基础网络能力 **6 项已实现或接近实现**，**9 项部分实现**，**14 项缺失或仅有外部入口**。此统计按能力模块而非页面数量计算，不能当作代码覆盖率。

最明显的缺口不是 MaxMind/IPHub，而是目标项目的工具箱宽度：**任意 IP 查询、IP 历史、真正的 WebRTC/DNS Leak、完整测速、Globalping 全球探针、MTR、代理规则、审查检查、MAC、ASN 历史/上游拓扑、服务官方状态、报告分享、Curl API、Earth Online、安全清单、指纹/Persona/Invisibility 等**。

## 逐项差异矩阵

| 目标能力 | MyIP / ipcheck.ing 公开行为 | MyIPCheck 当前状态 | 差异与判断 | 优先级 |
|---|---|---|---|---:|
| 当前 IP 信息 | 多探针 IPv4/IPv6；国家、地区、城市、ASN、组织、时区；可查询任意 IP | 已实现当前 IPv4、可用 IPv6、地理、ISP、ASN | 当前出口基本实现；**任意 IP 查询缺失**，数据源选择器也未实现 | P0 |
| IP 历史 | 浏览器本地记录 IP，可按类型和国家过滤 | 未实现 | 无本地历史数据库/筛选页面 | P1 |
| 网络连通性 | 最多约 60 个自选网站，多轮最小延迟，支持导入分类清单 | 部分实现 | 当前为固定少量站点和单轮 443/HTTPS 探测；无用户添加、分类导入、多轮最小值 | P1 |
| WebRTC 泄漏 | 多个 STUN 服务器、候选地址、NAT 类型、ISP/地区、SDP Log | 仅外部浏览器入口/备用卡片 | Android 原生不能等价复现浏览器 WebRTC/SDP 环境；不能标为原生已实现 | P1（网页）/P2（原生） |
| DNS Leak Test | 随机域名、多 DNS Endpoint、解析地域与 ISP | 部分实现 | 目前展示 Android Private DNS/DNS 服务器；不是递归 DNS 泄漏实验，也无多端点地理判定 | P1 |
| 浏览器指纹 / Browser Information | Canvas/WebGL、JS、浏览器信息和指纹 | 仅外部网页入口 | 原生应用没有相同浏览器指纹上下文；需要 WebView/外部网页或明确降级 | P2 |
| Invisibility Test | 独立检查是否使用代理/VPN | 部分实现 | v3.1 网络透明度/纯净度可提示代理、VPN、Tor，但没有独立工具页面和目标站点语义 | P2 |
| Security Checklist | 258 项、12 个领域、浏览器本地保存进度 | 未实现 | 无清单数据、领域筛选或进度持久化 | P2 |
| Speed Test | Cloudflare Edge 下载、上传、延迟、Jitter，可选包大小 | 部分实现 | 当前仅执行 Cloudflare 单次 HTTP 延迟，不进行上下行吞吐和 Jitter 测量 | P1 |
| Global Latency Test | Globalping 全球探针，从多国探针 Ping 目标 | 未实现 | 无 Globalping API、国家/大洲选择器或全球探针结果 | P1 |
| MTR Test | Globalping/全球探针路径追踪 | 未实现 | 无远端 MTR 任务、跳数、丢包、ASN 路径可视化 | P1 |
| Proxy Rule Test | 验证代理软件规则是否按预期工作 | 未实现 | 无规则输入、目标分流和规则结果报告 | P2 |
| Censorship Check | 多国检查网站是否被封锁及阻断方式 | 未实现 | 无 OONI/远端探针检查与国家维度结果 | P1 |
| DNS Resolution | 多解析器并行解析，按国家分组；更深模式含 ECS/DNSSEC | 部分实现 | 当前是 Android 系统解析器查询一个目标；无多解析器、国家分组、ECS/DNSSEC | P1 |
| Whois Search | 域名/IP Whois 查询 | 已实现基础版 | 已有 IANA/注册表链式查询和结果展示，但与上游丰富字段/多注册局体验仍有差距 | P1 |
| MAC Lookup | 查询物理地址厂商和详情 | 未实现 | 无 MAC 输入、OUI 数据源或厂商展示 | P2 |
| ASN Info | ASN 详情 | 部分实现 | ASN 作为当前 IP 信息字段展示；无独立 ASN 详情页 | P1 |
| ASN 历史与上游拓扑 | 前缀历史公告、ASN 到 Tier 1 的上游路径 | 未实现 | 无 BGP/拓扑数据源、历史时间线或图形 | P2 |
| Service Status | 官方状态页、实时可用性和近期事故 | 部分实现 | 当前是若干常用服务 443 端口连通性/耗时；不是官方 status page/incident 聚合 | P1 |
| Shareable Reports | 只读可过期链接、Markdown、JSON | 未实现 | 无后端报告存储、过期 token、导出 Markdown/JSON | P1 |
| Curl API | `curl` 获取 IP | 未实现 | Android APP 没有公开兼容 API 或部署端点 | P2 |
| Earth Online | 全球互联网中断事件广播 | 未实现 | 无 outage feed、地图/实时事件面板 | P2 |
| Dark Mode | 跟随系统并可手动切换 | 部分实现 | 当前有固定 Compose 主题；未确认完整系统跟随/手动切换 | P2 |
| PWA / Chrome App | 可安装网页/PWA | 架构上不适用 | Android APK 不是 PWA；若要复刻需另建 Web 前端 | P2（另项目） |
| Keyboard Shortcuts | `?` 查看快捷键，工具有键盘操作 | 未实现 | 移动端无快捷键体系 | P2 |
| 多语言 | 公开项目当前 6 种语言并支持 locale pack | 未实现 | MyIPCheck 当前主要为简体中文，未见 locale 资源体系 | P1 |
| AI/IPilot | 首页提供 Ask IPilot | 未实现 | 无 LLM 问答入口或报告解释助手 | P2 |
| In-depth DNS Leak | ECS、DNSSEC、全部递归解析器 | 未实现 | 基础 Private DNS 展示不能算深度 DNS Leak | P2 |
| In-depth Persona Check | 对比网站看到的信息与目标地区 | 未实现 | 无浏览器/多站点 persona 对比与报告 | P2 |
| 授权与风险数据源 | MyIP 服务器侧配置 MaxMind GeoLite2 等；本项目可选 AbuseIPDB、ipapi.is、MaxMind Insights、IPHub | 已实现本地授权子集 | 已加入本地加密 Key、眼睛显示/隐藏和 v3.1 风险信号；不等于上游服务端配置和全部数据源覆盖 | P0 |

## 当前 MyIPCheck 已实现的真实能力

源码当前的主页面组合包含 IP 信息、IPv6、增强纯净度诊断、连通性、Android 隐私/DNS、轻量网络测量、DNS 查询、Whois 查询、服务端口探测、设备环境和浏览器备用入口。网络仓库包含 `ipapi.co`、`ipwho.is`、ProxyCheck、Tor Project、AbuseIPDB、ipapi.is、MaxMind GeoIP Insights、IPHub 和 IANA/Whois 查询探针。

这意味着 MyIPCheck 不是空壳，也不是只改了界面；但它目前的实现方式是**原生移动端安全子集**，而不是把目标网站所有浏览器工具和后端探针直接移植到 Android。

## 最值得优先补齐的功能

### P0：纠正核心信息的复刻完整度

第一，应补充“查询任意 IP”入口，使用户可以输入 IPv4/IPv6 并调用公开地理/ASN数据源，同时将当前 IP 卡和查询结果模型分开。第二，应把授权设置和 IP 数据源选择明确关联：无 Key 使用公开默认源；有 MaxMind 等 Key 才启用增强字段。第三，应在首页增加功能状态/覆盖度说明，避免用户把当前 IP 卡误认为完整的 IPCheck.ing 信息卡。

### P1：形成真正可用的网络诊断工具箱

建议先实现 IP 历史、本地可编辑连通性列表、完整上下行测速、真正的多解析器 DNS 检查、独立官方服务状态和更完整 Whois。随后再接入 Globalping 的全球延迟与 MTR；这些功能依赖远端任务、配额、超时、探针选择和结果回放，不能用本地 `Socket` 探测伪装。

WebRTC Leak 和深度 DNS Leak 应优先以**可信 WebView/外部网页入口**实现，或新增一个明确的浏览器端 companion，而不是在原生 Android 中声称检测到了 JavaScript/SDP/Canvas 指纹。若目标是“功能复刻”而非“原生复刻”，最经济的方案是 Android 应用内嵌经过审计的 ipcheck.ing 工具页，并明确第三方网页隐私边界。

### P2：补齐平台和高级工具

安全清单、MAC Lookup、代理规则、审查检查、ASN 历史/上游拓扑、报告分享、Curl API、Earth Online、多语言、深色模式、快捷键和 In-depth Persona 是第二阶段。它们多数需要较大的数据、后端服务或 Web UI，不适合继续堆在单个 `MainActivity.kt` 中；应拆为模块化 Compose screen、Repository 和可测试的数据模型。

## 架构建议

当前几乎所有 Android UI、网络请求、数据模型和评分逻辑集中在 `MainActivity.kt`，这适合原型，但不适合继续复刻上游工具箱。建议拆成：`feature/ipinfo`、`feature/connectivity`、`feature/leaks`、`feature/advanced`、`feature/report`、`data/provider` 和 `domain/scoring`。每个 Provider 应返回带来源、时间、字段覆盖和错误状态的标准结果；UI 不应直接解析 JSON。

全球探针、报告分享、官方状态和 OONI 等功能应通过后端代理或受控服务端完成，避免把第三方 token、跨域逻辑和速率限制直接放入 APK。对于本地公共源，必须保留超时、429、字段缺失和隐私说明；对于 MaxMind、IPHub 等授权源，必须坚持“空 Key 回退公共源，而不是空 Key 调用授权端点”。

## 参考资料

[1] [jason5ng32/MyIP GitHub repository](https://github.com/jason5ng32/MyIP)

[2] [IPCheck.ing live demonstration](https://ipcheck.ing/)

[3] [MyIP repository API tree](https://github.com/jason5ng32/MyIP/tree/main/api)

[4] [MyIP repository frontend tools](https://github.com/jason5ng32/MyIP/tree/main/frontend/components/advanced-tools)

[5] [Globalping documentation](https://globalping.io/docs)

[6] [OONI Explorer](https://explorer.ooni.org/)

[7] [MaxMind GeoIP Web Services](https://dev.maxmind.com/geoip/docs/web-services/)
