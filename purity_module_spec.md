# 增强透明纯净度诊断模块规格 v2.1

## 产品定位

“增强透明纯净度诊断”是 MyIPCheck 的**独立、可解释公开网络出口风险信号指数**。它参考公开 IP 风险服务的字段语义、分段和误报控制原则，但不复制、反推或声称使用任何厂商的专有算法、人工 IP 段标注、服务器或商业风险数据库。[1] [2] [3]

> **100.0 分**仅表示本次成功覆盖的公开来源中未见可计入风险的信号。它不是绝对安全、欺诈概率、账号信誉、个人评价或业务决策结论；没有真实结果标签时，不能声称模型已概率校准。[4]

## 评分结构

APP 以 `Double` 进行计算，并显示一位小数：

```text
总风险 = clamp(匿名化网络 + 滥用与攻击 + 托管基础设施 + 观测完整性, 0, 100)
总分   = 100 - 总风险
```

| 风险桶 | 上限 | 主要输入 | 误报控制 |
| --- | ---: | --- | --- |
| 匿名化网络 | 45.0 | Tor、代理、VPN、ProxyCheck 置信度与最近检出时间 | 同一匿名化事实只取最强结论，双来源只给有限支持。 |
| 滥用与攻击 | 40.0 | AbuseIPDB 原始分、报告数与时效；ProxyCheck 风险、置信度、攻击历史；公开布尔标记 | 同类供应商结果取最强值，独立来源支持最高仅 4.0。 |
| 托管基础设施 | 12.0 | ASN / 组织启发式、托管、数据中心与 ProxyCheck 置信度 | 托管不等同恶意，严格限制为轻量提示。 |
| 观测完整性 | 8.0 | 同次检测的出口 IPv4 冲突 | 国家库和双栈地理差异不参与风险。 |

任何来源未配置、接口超时、配额耗尽、未知值或字段未覆盖都会显示为“未覆盖”，**绝不扣分**。

## 连续数值规则

当来源返回数值或时间时，APP 不再按 25 / 50 / 75 等固定区间跳档。设信号距今天数为 `d`，时效系数为：

```text
F(d) = clamp(0.45 + 0.55 × e^(-d / 21), 0.45, 1.00)
```

ProxyCheck 返回置信度 `c`（0–100）时：

```text
R = clamp((0.30 + 0.70 × c / 100) × F(最近检出日), 0.25, 1.00)
```

`R` 只表示本模块对 ProxyCheck 当前信号的缩放可靠度，并非用户身份或代理概率。[3]

| 原始输入 | 逐分映射 |
| --- | --- |
| AbuseIPDB 分 `a` | `34.0 × (a / 100)^1.35` |
| AbuseIPDB 报告数 `n` | `2.0 × ln(1+n) / ln(101)` |
| AbuseIPDB 时效 | `2.0 × F(最近报告日)`，仅当 `a > 0` |
| AbuseIPDB 合计 | 三项相加后最高 `36.0` |
| ProxyCheck 风险 `p` | `16.0 × (p / 100)^1.20 × R` |
| ProxyCheck 攻击事件数 `h` | `4.0 × ln(1+h) / ln(101) × R` |
| ProxyCheck 托管标记 | `4.0 + 3.0 × R` |

因此，AbuseIPDB 从 30 到 31、ProxyCheck 从 40 到 41，或同一风险在不同置信度和不同最后发现日期下，都会造成不同的小数风险和总分。AbuseIPDB 的 Check 端点公开返回 `abuseConfidenceScore`、`totalReports` 和 `lastReportedAt`；ProxyCheck v3 公开返回 0–100 `confidence`、`risk`、`last_seen` 和攻击历史。[2] [3]

## 仅有布尔值的证据

有些公开接口只提供“是 / 否”，没有足以细分的原始数值。此类信号使用明确基准权重，并在 APP 明细中说明限制；不会捏造小数概率。

| 二元证据 | 规则 |
| --- | --- |
| Tor Project 官方确认 | `45.0`，匿名化桶上限。 |
| 其他来源 Tor | `38.0`；若 ProxyCheck 同时确认，加 `4.0 × R`。 |
| 仅 ipapi.is 代理 / VPN | `28.0 / 22.0`。 |
| ipapi.is `is_abuser=true` | `16.0`。 |
| ipapi.is `is_crawler=true` | `4.0`。 |
| ASN / 组织托管关键词 | `3.0`。 |
| ipapi.is `is_datacenter=true` | `6.5`。 |
| 同次出口 IPv4 不一致 | `8.0`。 |

代理 / VPN 的 ProxyCheck 路径、双源交叉验证、受损、爬虫和托管会用 `R` 连续缩放。所有结果会先在风险桶内去重和封顶，再汇总，避免同一网络事实被多个供应商线性重复扣分。

## 不计分信号

| 信号 | 处理方式 |
| --- | --- |
| `egress_service` 受管理出口 | 仅展示。 |
| Android VPN 与 Private DNS 状态 | 仅展示。 |
| 国家代码差异 | 仅展示。 |
| IPv4 / IPv6 国家差异 | 仅展示。 |
| Key 未配置、失败或字段缺失 | 显示“未覆盖”，不扣分。 |

## 展示档位

| 总分 | 标签 | 含义 |
| ---: | --- | --- |
| 90.0–100.0 | 低风险信号 | 已覆盖来源未见明显风险信号；未覆盖不是安全结论。 |
| 70.0–89.9 | 轻度提示 | 存在有限、可解释的网络属性或行为提示。 |
| 40.0–69.9 | 需复核 | 发现较强匿名化、滥用或出口观测信号。 |
| 0.0–39.9 | 高风险提示 | 多个独立风险桶同时命中；不可单独据此推断个人、账号或交易。 |

报告显示每个风险桶的“实际风险 / 上限”，并在明细中显示原始分、连续换算结果、置信度 / 时效输入和有限交叉验证增量。

## 数据、隐私与校准边界

诊断按需调用 `api.ipify.org`、`ipapi.co`、`ipwho.is`、`proxycheck.io` 和 Tor Project。用户自行配置 Key 后，APP 还会调用 AbuseIPDB APIv2 Check 与 ipapi.is 官方 JSON POST 端点。[2] [5] [6]

调用仅使用当前公网 IP 或由服务自动观察到的请求来源 IP；APP 不传递账号、Cookie、浏览历史、设备指纹、位置权限或其他个人敏感信息。AbuseIPDB、ipapi.is 和自定义预留 Key 使用 Android Keystore 的 AES-GCM 密钥加密后仅存于本机；预留 Key 当前不会发送或参与评分。

这些权重是基于公开字段语义、分段和误报控制设计的**可解释先验**，不是经 MyIPCheck 用户行为训练的统计模型。后续只有在合法、最小化、经授权的真实结果数据下，才应通过校准曲线和观察事件率调整系数。[4]

完整公式、情景测试和引用请见 [`purity_scoring_model_v2.md`](purity_scoring_model_v2.md)。

## 参考

[1] [GetIPIntel Detection API](https://getipintel.net/free-proxy-vpn-tor-detection-api/)

[2] [AbuseIPDB APIv2 Documentation](https://docs.abuseipdb.com/)

[3] [ProxyCheck v3 API Documentation](https://proxycheck.io/api/)

[4] [Crowson et al., Assessing Calibration of Prognostic Risk Scores](https://pmc.ncbi.nlm.nih.gov/articles/PMC3933449/)

[5] [ipapi.is Developers Documentation](https://ipapi.is/developers.html)

[6] [Tor Project IP API](https://check.torproject.org/api/ip)
