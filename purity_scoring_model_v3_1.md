# MyIPCheck 公开网络出口风险模型 v3.1

**状态：** 可解释的公开滥用风险信号指数；不是欺诈概率、个人信誉、账号风控结论或任何第三方的专有评分。  
**版本目标：** 将历史/公开滥用证据与匿名化、基础设施、出口可观测性分离，并将每一个显示分值关联到可查看的来源、字段和时间。

## 1. 输出与不变量

| 输出 | 范围与方向 | 进入主分 | 含义 |
|---|---:|---:|---|
| `R` 公开滥用风险信号 | 0–100，高表示直接或间接的公开滥用证据更强 | 是 | 当前已覆盖来源中观察到的历史/公开行为证据强度；不是未来概率。 |
| `P` 纯净度展示分 | `100 − R`，高表示已识别滥用信号更少 | 是 | 仅是 `R` 的反向展示，不能称安全保证。 |
| `A` 网络透明度 | 0–100，高表示 Tor/代理/VPN/中继/多出口异常更明显 | 否 | 当前网络路径的匿名化或可归因性提示，不是历史恶意。 |
| `X` 网络上下文 | 标签和来源详情 | 否 | IDC、托管、ASN、前缀和 ISP 等背景信息，不推断恶意。 |
| `C` 证据覆盖度 | 0–100，高表示关键证据通道可用、字段完整且新鲜 | 否 | 本次评估的可见性，不代表 IP 安全。 |

> **硬性不变量：** Key 未配置、超时、429、字段缺失、格式异常、“无记录”、无法解析或来源过期，均不能降低 `R`；它们仅降低 `C` 或显示为未覆盖。Tor、VPN、代理、IDC、云、国家、ASN、地理或出口差异均不能单独提高 `R`。

## 2. 默认端点与授权边界

| 来源 | 默认端点或入口 | Key 状态 | 在模型中的位置 |
|---|---|---|---|
| ipify / ipapi.co / ipwho.is | 现有公开 HTTPS 端点 | 无 Key | 当前 IP 与基础地理交叉核验；仅对出口观测和覆盖度有意义。 |
| ProxyCheck | `https://proxycheck.io/v3/{ip}?vpn=1&asn=1&risk=1` | 无 Key（受服务配额限制） | 代理/VPN/Tor/托管进入 `A`；受损、攻击历史与风险字段作为低至中强的公开行为证据候选。 |
| Tor Project | `https://check.torproject.org/api/ip` | 无 Key | 官方 Tor 出口确认，只进入 `A`。 |
| AbuseIPDB | `https://api.abuseipdb.com/api/v2/check?ipAddress={ip}&maxAgeInDays=90` | 用户配置 `Key` 请求头 | 置信分、报告数量、独立报告者数与最近报告时间，进入“泛化滥用”行为家族。 |
| ipapi.is | `POST https://api.ipapi.is` | 用户配置 Key | `is_abuser` 与 `is_crawler` 按家族进入 `R`；代理/VPN/Tor/数据中心只进入 `A/X`。 |
| MaxMind GeoIP Insights | `GET https://geoip.maxmind.com/geoip/v2.1/insights/{ip}` | 用户配置 Account ID + License Key，HTTPS Basic Auth | `anonymizer` 与 `traits` 进入 `A/X` 和覆盖度；不作为历史滥用证据。 |
| IPHub v2.2 | `GET https://v2.api.iphub.info/ip/{ip}` | 用户配置 `X-Key` | `block==1`、proxyType 进入 `A/X`；`block==2` 显示低置信提示，不进入 `R`。 |
| BrowserLeaks | `https://browserleaks.com` 浏览器入口 | 无公共 Android API | 仅作为网页专属 WebRTC、JS、Canvas/WebGL、TLS/DNS 自检入口。 |
| myip.edgeone.ai | 用户指定网址 | 未发现可解析主机/API | 不发起默认请求；在来源状态中标为“当前不可用，待提供官方接口”。 |
| NSTool | `https://nstool.netlify.app` | 未公开风险 API | 仅可打开站点说明；不下载 APK，不纳入评分或默认 API 请求。 |

MaxMind 与 IPHub 的端点在应用中预置，用户只需填入自身账号密钥即可启用。所有 Key 仅以 Android Keystore AES-GCM 加密保存在当前设备，绝不拼接在 URL、上传至项目服务器或写入日志。

## 3. 行为家族与逐分公式

v3.1 的每一分都由下列连续函数得到，而不是由模糊的固定档位跳变。每个来源返回的原始记录先映射为带有来源谱系、行为家族、计数、时间和分辨率的证据。

```text
V(n; τ) = 1 − exp(−n / τ)                         # 计数饱和
T(d; h) = 2^(−d / h)                              # 时间半衰期
E(g,s)  = M_g × Q_s × V(n; τ_g) × T(d; h_g) × L   # 单源家族证据
```

其中，`M_g` 是严重度上限，`Q_s` 是首发版本中依据字段粒度和审计能力设定的来源质量上限，`L` 是分辨率系数（IP=1.00、精确前缀=0.40、ASN=0.15）。`Q_s` 是可被后续真实标签回测替换的**产品先验**，绝不是供应商正确率或 IP 恶意概率。

| 行为家族 | `M_g` | `τ_g` | `h_g`（天） | 初始可用来源 |
|---|---:|---:|---:|---|
| 受损/恶意软件基础设施 | 0.95 | 1 | 120 | ProxyCheck `compromised`；未来可接入类别明确的 IOC 源。 |
| 漏洞/凭据攻击 | 0.80 | 3 | 45 | ProxyCheck 攻击历史（类别未知时降级为泛化滥用）。 |
| 泛化滥用 | 0.60 | 5 | 60 | AbuseIPDB 置信、报告和独立报告者；ipapi.is `is_abuser`。 |
| 扫描/爬虫 | 0.20 | 5 | 14 | ipapi.is `is_crawler`；ProxyCheck `scraper`。 |
| 无类别供应商总风险 | 0.25 | 5 | 30 | ProxyCheck `risk`，严格限权。 |

每一个家族只保留相同来源谱系内的最强证据；跨谱系的有限交叉支持不得超过 20%。然后以非线性组合构成主风险：

```text
E_g = min(0.95, max_s E(g,s) × [1 + 0.10 × min(k_g − 1, 2)])
R   = 85 × [1 − Π_g (1 − E_g)]
P   = 100 − R
```

`k_g` 是被人工标注为独立的来源谱系数量。初版将不确定谱系一律视为相关，因此不给额外交叉加分。85 分的上限保留了不确定性；只有以外部、时间外真实标签得到校准验证后，才可调整预算或声明为概率。

## 4. 网络透明度、上下文与覆盖度

`A` 独立显示：Tor Project 官方确认可取 100；多源 Tor 为 95；多源代理为 75；单源代理为 60；多源 VPN 为 55；单源 VPN 为 45；IPHub 的 `block==2` 为“低置信可疑”标签而非加分。这些数值只是透明度提示的连续可解释标尺，绝不与 `R` 相加。

`X` 独立显示 MaxMind/ IPHub/ ProxyCheck 的网络类型、ISP、ASN、CIDR、托管/中继与证据来源。基础设施属性默认不计分。出口 IPv4 不一致被显示为“出口观测差异”，不计入 `R`。

```text
C = 100 × Σ_j [w_j × availability_j × completeness_j × freshness_j] / Σ_j w_j
freshness_j = max(0, 1 − age_j / TTL_j)
```

初始通道权重：公开滥用 55、匿名化 25、网络上下文 15、出口观测 5。`availability`、`completeness` 和 `freshness` 可为 0–1；无记录仅说明该通道成功响应，不能当作“安全”。当 `C<60` 时，主卡必须显示“证据不足，未发现不等于无风险”。

## 5. 校准与验证路线

此规则是无自有真实标签时可审计、可回放的 v3.1 启发式。若将来合法获得某一明确业务 `b` 中未来 30 天是否发生目标滥用的结果，必须使用评分时点之后的标签、按时间滚动切分并分组防止同 IP/前缀/ASN 泄漏；再以独立校准集评估 PR-AUC、Precision@K、阈值误报率、校准曲线、校准截距/斜率及 Brier 分数。未经这些步骤，APP 不得将 `R`、`P` 或 `A` 表述为发生概率。

## 参考

- MaxMind GeoIP Web Services Requests / Responses: https://dev.maxmind.com/geoip/docs/web-services/requests/ 与 https://dev.maxmind.com/geoip/docs/web-services/responses/
- IPHub API: https://iphub.info/api
- AbuseIPDB API: https://docs.abuseipdb.com/
- CrowdSec CTI Taxonomy: https://docs.crowdsec.net/u/cti_api/taxonomy/intro/
- 源文件审计：`docs_external_sites_assessment.md`、`docs_maxmind_iphub_integration.md` 与 `IP纯净度评分规则核验与v3_1建议.md`
