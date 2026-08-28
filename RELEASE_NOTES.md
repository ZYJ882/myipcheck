## v1.0.22：Globalping MTR、DNS.SB 与工具箱增强

本版本继续一次性吸收适合 Android 原生端可靠实现的公开能力，并保持所有远端测量由用户手动触发、受限规模执行。

| 新增或升级 | 实现范围与边界 |
|---|---|
| Globalping MTR | 从美国、德国、新加坡各选择 1 个远端探针，执行 ICMP MTR，最多轮询 4 次并展示最多 64 跳的主机、IP、ASN、丢包与延迟字段；这是远端探针到目标的路径观测，不是手机本机路由，也不把中间跳 ICMP 限速直接判为故障。 |
| DNS.SB resolver | DNS 交叉查询新增上游公开的 DNS.SB DoH 解析器；结果仍只表示多解析路径答案对比，不是深度 DNS Leak、ECS、DNSSEC 或劫持的确定性判断。 |
| 生产力 HTTPS 预设 | 连通性设置可一次加入 Microsoft 365、Slack、Notion、Atlassian、Dropbox、Zoom、Figma、GitLab、npm、PyPI、AWS 和 Docker Hub 等预设，仍受最多 12 项限制，保存后需手动探测。 |
| 安全清单搜索 | 30 项离线自评支持按领域、标题和建议内容搜索；搜索和勾选均为本地操作，不读取系统设置、不扫描应用、不上传完成进度。 |
| 本地报告扩展 | Markdown/JSON 摘要现在可包含受限 MTR 的结构化探针和逐跳数据；仍只通过 Android 系统分享面板输出，不建立在线报告。 |

> Globalping、DNS.SB、DoH、OONI、RIPEstat、状态页和端口/HTTPS 目标请求均不在后台运行。第三方服务的速率、可用性和返回结果可能变化，应用将错误与未覆盖状态保留为诊断结果的一部分。

## v1.0.21：离线安全清单、主题与双格式导出

本版本优先实现不依赖浏览器指纹、私有威胁库或后端服务的 Android 原生能力：本地安全自评、显示偏好和由用户主动发起的诊断结果导出。

| 新增或升级 | 实现范围与边界 |
|---|---|
| 离线安全清单 | 新增 30 项设备、账户、网络、隐私和应急自评建议；进度使用 Android Keystore AES-GCM 仅加密保存于本机，可随时清除。它不读取系统设置、不扫描应用，也不是安全认证或自动风险评估。 |
| 显示主题 | 可选择跟随系统、始终浅色或始终深色；选择加密保存在本机，主题仅改变界面，不改变检测结果或网络请求。 |
| Markdown / JSON 导出 | 顶部分享入口可选 Markdown 兼容文本或结构化 JSON 摘要，并通过 Android 系统分享面板在用户操作时输出。导出不含授权 Key、加密 IP 历史或浏览器指纹数据。 |
| 本地数据隔离 | 修正“清除授权 Key”的存储范围，使其仅移除授权凭据，不再同时删除独立的 IP 历史、端口目标、清单进度或显示偏好。 |

> 安全清单代表用户自己的完成确认，不代表系统自动检测。JSON 中的网络、DNS、连通性、端口和官方状态均是当时可用的本地/公开数据快照，必须结合相应来源边界解释。

## v1.0.20：自定义端口、DNS 共识与系统网络能力

本版本继续完善适合 Android 原生端的 MyIP / IPCheck.ing 诊断功能，并把主动探测范围限制为用户配置的单主机单端口，避免将实用网络检测扩展为端口范围扫描。

| 新增或升级 | 实现范围与边界 |
|---|---|
| 端口目标管理 | 可添加、删除和恢复默认端口探测目标；每项限制为单主机加单端口，端口限于 1–65535，最多 12 项，加密存储在本机。 |
| DNS 共识与导出 | 根据系统解析与可用 DoH 解析器返回结果给出“一致 / 有差异 / 无记录 / 未覆盖”提示，可通过 Android 系统分享文本结果。差异不等于 DNS 泄漏。 |
| 官方状态 | 新增 Discord 官方公开状态摘要；继续单独显示 GitHub、Cloudflare、OpenAI、Discord 的官方全球状态和手机本机端口可达性。 |
| Android 网络环境 | 扩展为传输类型、系统互联网验证、按流量计费提示、网络接口、Private DNS 主机名及系统上报的上下行带宽估算。系统估算不等于实测带宽。 |

> 端口探测的目标、ASN/DNS/OONI/Globalping 查询和状态页请求均仅在用户操作时发起；不做后台持续监控。新增状态页仅使用厂商公开接口，其内容和可用性受各厂商维护状态影响。

## v1.0.19：高级 DNS、ASN 拓扑与按需远端测量

本版本继续复刻 MyIP / IPCheck.ing 的高级诊断方向，并把公开远端 API 的使用限制直接写入界面，避免把远端或历史结果误说成本机即时检测。

| 新增或升级 | 实现范围与边界 |
|---|---|
| DNS 记录类型 | DNS 工具新增 A、AAAA、TXT、MX、NS、CNAME 输入；系统解析仅对 A/AAAA 可用，其余类型由 Cloudflare、Google Public DNS、Quad9 的 DoH 交叉返回。 |
| ASN 邻居 | ASN 查询同时读取 RIPEstat ASN Neighbours，显示 RIS 可见的独立邻居数和最多 12 条邻居/路径位置。`left`/`right` 是 AS 路径位置，不等于商业上游、下游或客户关系。 |
| Globalping | 用户手动点击后，从美国、德国、新加坡各选择 1 个探针 ping 目标；单次最多消耗 3 个免费测试，不自动运行。结果表示探针到目标的远端测量。 |
| OONI 历史 | 用户可输入域名与国家代码，读取最多 5 条 Web Connectivity 历史测量元数据，并显示 anomaly、confirmed、failure 与 verification 状态。它不是本机实时封锁检测。 |
| 高级 API 文档 | 新增 `docs_advanced_public_api_boundaries.md`，记录端点、受控请求规模、公共额度与解释约束。 |

> Globalping 的免费额度按探针测试计，并可能变化；OONI API 具有速率限制且不适合批量抓取。应用不轮询后台任务、不自动提交测量，仅在用户主动操作时发起受限请求。

## v1.0.18：连通性管理、ASN / MAC 与官方状态摘要

本版本继续复刻 MyIP / IPCheck.ing 中能在 Android 原生端可靠实现的工具，并严格区分本机探测、公开登记数据与厂商的官方全球服务状态。

| 新增或升级 | 实现范围与边界 |
|---|---|
| 连通性目标管理 | 可添加、删除、恢复默认的 HTTPS 探测目标，最多 12 项；目标清单经 Android Keystore AES-GCM 加密后仅存本机。 |
| ASN 信息 | 通过 RIPE NCC RIPEstat `as-overview` 获取 ASN 持有人、RIR/IANA 分配块和可见前缀状态；它不是完整路由拓扑或历史。 |
| MAC Lookup | 通过 MACVendors 查询 MAC/OUI 前缀厂商登记；检测到本地管理位时不进行厂商归因，避免将随机/虚拟 MAC 误报为厂商地址。 |
| 官方状态摘要 | 从 GitHub、Cloudflare、OpenAI 的公开状态页 API 获取当前全局摘要，与 443 端口探测单独展示。 |
| 操作可靠性 | 修正历史清除、纯净度重测和连通性管理章节操作的点击回调。 |

> MACVendors 的匿名接口有公开频率与每日额度限制；RIPEstat、厂商状态页和第三方 DNS/测速结果均可能有延迟或短暂不可用。APP 将这些情况显示为来源错误或未覆盖，不将其解释为网络或服务故障。

## v1.0.17：首批 Android 原生工具复刻

本版本开始复刻 MyIP / IPCheck.ing 中适合 Android 原生实现的工具能力，同时明确保留浏览器和全球探针功能的边界。

| 新增或升级 | 实现范围 |
|---|---|
| 查询 IP | 支持输入 IPv4 / IPv6 查询位置、时区、ISP、ASN 和 IP 版本。 |
| 加密 IP 历史 | 当前出口与手动查询最多保存 30 条，使用 Android Keystore AES-GCM 本地加密，支持一键清除。 |
| 多解析器 DNS | 同时显示 Android 系统解析及 Cloudflare、Google Public DNS、Quad9 的 DNS over HTTPS 交叉结果。 |
| 限量测速 | 用户手动开始后测量 Cloudflare Edge 的中位延迟、抖动和最多 1 MB 下载吞吐；不上传数据、不会自动运行。 |
| 分享摘要 | 使用 Android 系统分享面板导出当前网络、风险和测速信息的 Markdown 兼容纯文本；不上传、不创建公开链接。 |
| 网页工具入口 | 增加 BrowserLeaks、IPCheck WebRTC Leak、DNS Leak 和高级工具入口；网页数据不回写 APP，亦不参与纯净度评分。 |

> 完整 WebRTC/浏览器指纹、DNS Leak、Globalping 全球延迟/MTR、OONI 审查检查、可过期的公开报告和 Earth Online 需要浏览器环境、远端探针或后端服务，尚未伪装为 APP 内置结果。

## v1.0.13：统一服务商授权与默认公共检测回退

本次修订将“授权数据源 Key”调整为统一的服务商管理界面。未填写任何 Key 时，APP 明确显示并自动使用 **ipify、ipapi.co、ipwho.is、ProxyCheck 和 Tor Project** 等可公开访问的默认检测源；填写可选服务商凭据后，才在本机按需叠加相应结果。

| 调整项 | 行为 |
|---|---|
| 默认公共检测 | 置顶显示“已启用”；即使不配置 AbuseIPDB、ipapi.is、MaxMind 或 IPHub，也可完成基础检测。 |
| 可选授权服务商 | AbuseIPDB、ipapi.is、MaxMind GeoIP Insights 和 IPHub 集中显示在同一授权区域；空 Key 时不请求这些官方要求授权的服务。 |
| 网页 / 非 API 服务 | BrowserLeaks 标为网页入口；EdgeOne MyIP 标为当前不可用；NSTool 标为未提供 API，均不伪装为公共 API。 |
| 密钥交互 | 每个 Key 及 MaxMind Account ID 仍有独立的显示/隐藏图标，并仅在本机加密保存。 |

> 这次变更不代表 MaxMind 或 IPHub 可以无 Key 调用。它们的官方 API 均要求授权凭据；“默认公共检测”是合法可用的公开来源回退，而非绕过任何服务商授权。

## v1.0.12：多源 IP 情报与可回放风险评分

本版本将 MyIPCheck 的授权数据源扩展为 **AbuseIPDB、ipapi.is、MaxMind GeoIP Insights 与 IPHub v2.2**，并将展示逻辑升级为可回放的 **v3.1 公开网络出口风险模型**。更新保持“风险信号指数”定位，不把主分表述为账号信誉、欺诈概率或第三方专有风控值。

| 更新项 | 说明 |
|---|---|
| MaxMind GeoIP Insights | 预置 `https://geoip.maxmind.com/geoip/v2.1/insights/{ip}`；通过本地配置的 Account ID 与 License Key 使用 HTTPS Basic Auth。匿名化与网络字段只进入透明度、上下文和覆盖度。 |
| IPHub v2.2 | 预置 `https://v2.api.iphub.info/ip/{ip}`；通过本地 `X-Key` 和 `Accept-Version: 2.2` 调用。`block=1` 与 `proxyType` 作为透明度/上下文；`block=2` 仅显示低置信提示，不作为风险加分。 |
| Key 可见性控制 | 每个 Key 与 MaxMind Account ID 输入框均提供独立“小眼睛”，可由用户在本机即时显示/隐藏。全部凭据仍通过 Android Keystore AES-GCM 加密存储，不写入 URL、日志或仓库。 |
| v3.1 主分 | 主分仅反映公开滥用行为证据的反向展示；受损、攻击历史、已报告滥用、爬虫和严格限权的无类别供应商风险按来源质量、计数饱和、时间衰减和非线性组合连续计算。 |
| 网络透明度 | Tor、代理、VPN 和中继以 0–100 的独立透明度刻度展示；即使被识别，也不再直接降低“公开滥用风险”主分。 |
| 网络上下文 | IDC、托管、ASN、CIDR、ISP 与出口差异只作解释性信息，不用基础设施身份推断历史恶意。 |
| 覆盖度 | Key 未配置、配额耗尽、429、超时、响应失败与字段缺失降低证据覆盖度，但不会被静默解释为低风险。覆盖度低于 60 时，优先显示“证据不足”。 |
| 浏览器专属检测 | 增加 BrowserLeaks 外部自检入口；该类网页 JavaScript、WebRTC、Canvas/WebGL、TLS 和 DNS 检测不会被原生应用伪造或混入主分。 |
| 来源边界 | `myip.edgeone.ai` 在本次网络核验中无法解析，NSTool 未公开可审计 IP 情报 API；两者不会作为默认请求、评分来源或 APK 下载来源。 |
| 回归验证 | 更新 `scripts/validate_purity_model.py`，验证连续相邻分、时间衰减、透明度隔离、覆盖度缺失治理与输出范围。 |

本地调试构建使用 `versionCode 1012` 和 `versionName 1.0.12`。GitHub 发布工作流仍会在推送 `main` 后根据运行序号覆盖自动发布的版本名与版本码，并生成签名 APK 与 GitHub Release。

### 参考

- [MaxMind GeoIP Web Services Requests](https://dev.maxmind.com/geoip/docs/web-services/requests/)
- [MaxMind GeoIP Web Services Responses](https://dev.maxmind.com/geoip/docs/web-services/responses/)
- [IPHub API v2.2](https://iphub.info/api)
- [AbuseIPDB API Documentation](https://docs.abuseipdb.com/)
- [ProxyCheck API Documentation](https://proxycheck.io/api/)
