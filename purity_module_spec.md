# 增强透明网络出口风险诊断模块规格 v3.0

## 产品定位

“透明网络出口风险诊断”是 MyIPCheck 独立实现的**公开网络出口风险信号指数**。它使用可公开核验的字段语义、连续时间衰减、分层风险预算与可见覆盖度，不复制、反推或声称使用任何第三方的专有算法、人工 IP 段标注、行为数据库或商业风控值。[1] [2] [3]

> **主分 100.0** 仅表示本次已覆盖的来源中未识别到可计入的公开风险信号。它不是绝对安全、欺诈概率、个人/账号信誉、支付结论或业务决策建议。缺失来源与字段必须在“证据覆盖度”中反映，而不能被解释为无风险。

## 输出结构

APP 使用 `Double` 计算并显示一位小数。输出包含一个主分、四个有上限的风险层和一个独立覆盖度指标：

```text
总风险 = clamp(直接恶意与滥用 + 匿名化/透明度 + 网络上下文 + 出口可观测性, 0, 100)
主分   = 100 - 总风险

覆盖度 = Σ(风险层权重 × 该层关键字段覆盖状态) / 100 × 100
```

| 风险层 | 上限 / 覆盖权重 | 主要输入 | 误报控制 |
|---|---:|---|---|
| 直接恶意与滥用 | 70.0 | AbuseIPDB 原始分、报告量、时效；ProxyCheck 风险、受损、攻击历史；ipapi.is 滥用/爬虫 | 同类供应商结论不线性相加；不同来源仅形成有限交叉支持。 |
| 匿名化 / 透明度 | 15.0 | Tor、代理、VPN、ProxyCheck 置信度与最近检出时间 | 网络属性单列，不等同于历史恶意。 |
| 网络上下文 | 10.0 | ASN/组织启发式、托管、数据中心 | 仅作低上限先验；云/IDC/托管不等于恶意。 |
| 出口可观测性 | 5.0 | 同次 ipify 与 ipwho.is 返回的 IPv4 是否一致 | 仅反映当前观测路径差异，不推断历史行为。 |

## 连续计算原则

对于存在数值和时间字段的来源，APP 不按固定档位跳变。时效系数和 ProxyCheck 可靠度为：

```text
F(d) = clamp(0.45 + 0.55 × e^(-d / 21), 0.45, 1.00)
R    = clamp((0.30 + 0.70 × confidence / 100) × F(last_seen), 0.25, 1.00)
```

`R` 仅缩放 ProxyCheck 的自身信号，不表示用户身份或恶意概率。AbuseIPDB 原始分、报告量和报告时效分别进入连续计算；同一风险语义跨供应商使用“取最强证据 + 有限交叉支持”的饱和式聚合，避免重复扣分。

完整的可复算公式、风险预算、布尔证据处理、覆盖度定义与后续校准治理见 [`purity_scoring_model_v3.md`](purity_scoring_model_v3.md)。

## 字段缺失与来源状态

评分实现必须严格区分“字段明确返回 false/0”与“字段缺失、类型错误或响应不完整”。所有布尔和数值风险字段使用可空解析：

| 来源/字段状态 | 风险处理 | 覆盖度处理 | 页面状态 |
|---|---|---|---|
| 明确 `false` / `0` | 作为零风险输入参与计算 | 视为已覆盖 | 未检出 |
| 明确 `true` / 正数 | 按对应连续或布尔规则计入 | 视为已覆盖 | 命中风险提示 |
| 字段缺失、类型错误或响应不完整 | 不扣风险 | 对应层未覆盖 | 字段未覆盖 |
| Key 未配置、接口超时、配额耗尽或请求失败 | 不扣风险 | 对应层未覆盖 | 未配置 / 未覆盖 |

这样可以确保“未观测到”不会被静默显示为“无风险”。

## 不计入历史滥用风险的信号

| 信号 | 处理方式 |
|---|---|
| `egress_service` 受管理出口 | 仅展示。 |
| Android VPN 与 Private DNS 状态 | 仅展示。 |
| 国家代码差异 | 仅展示。 |
| IPv4 / IPv6 国家差异 | 仅展示。 |
| 自定义 HTTPS 地址与 Key | 当前仅加密保存，不发起请求、不参与评分。 |

## 展示规则

主分的标签与覆盖度分开解释：

| 条件 | 标签 | 含义 |
|---|---|---|
| 覆盖度 `< 50` | 关键证据覆盖不足 | 主分只反映已覆盖来源，不能据此判断安全性。 |
| 主分 `90–100` 且覆盖度 `≥ 50` | 低风险信号 | 已覆盖来源中未见明显公开风险信号。 |
| 主分 `70–89.9` | 轻度提示 | 存在有限、可解释的公开风险或透明度提示。 |
| 主分 `40–69.9` | 需复核 | 已发现较强风险、透明度或出口观测提示。 |
| 主分 `0–39.9` | 高风险提示 | 多个高权重公开风险信号同时命中。 |

报告必须显示各风险层“实际风险 / 上限”、原始字段、连续换算结果、时效/置信度输入、覆盖度和未覆盖原因。

## 数据、隐私与校准边界

诊断按需调用 `api.ipify.org`、`ipapi.co`、`ipwho.is`、`proxycheck.io` 和 Tor Project。用户本地配置 Key 后，APP 才调用 AbuseIPDB APIv2 Check 与 ipapi.is 官方 JSON POST 端点。[2] [4] [5]

APP 仅使用当前公网 IP 或由服务自动观察到的请求来源 IP；不发送账号、Cookie、浏览历史、设备指纹或位置权限。AbuseIPDB、ipapi.is 和自定义 HTTPS 请求配置的 Key 使用 Android Keystore AES-GCM 密钥仅在本机加密保存。

当前权重是基于公开字段语义、连续性与误报控制的**可解释先验**。未来只有在获得合法、最小化、经授权、独立于供应商结论的真实结果标签后，才应以时间外验证、校准曲线、Brier score、PR-AUC、误报率和漏报率来校准或更新参数。[6] [7]

## 参考

[1] [Spamhaus — IP Address Reputation](https://www.spamhaus.org/ip-reputation/)

[2] [AbuseIPDB APIv2 Documentation](https://docs.abuseipdb.com/)

[3] [CrowdSec — IP Range Reputation System](https://www.crowdsec.net/blog/introducing-the-ip-range-reputation-system)

[4] [ProxyCheck v3 API Documentation](https://proxycheck.io/api/)

[5] [ipapi.is Developers Documentation](https://ipapi.is/developers.html)

[6] [scikit-learn — Probability Calibration](https://scikit-learn.org/stable/modules/calibration.html)

[7] [NIST AI Risk Management Framework](https://www.nist.gov/itl/ai-risk-management-framework)
