# 增强透明网络出口风险诊断模块规格 v3.2（最终透明版）

## 产品定位

MyIPCheck 的“透明网络出口风险诊断”是独立设计的**公开网络出口风险信号指数**。它不复制、反推或声称使用任何第三方的专有算法、人工 IP 段标注、行为数据库或商业风控值。主分仅总结本次可观察到的公开行为证据；它不是欺诈概率、个人/账号信誉、支付结论、访问控制建议或安全保证。

> **“未发现”不等于“无风险”。** 每次结果必须同时展示主分、独立的网络透明度、网络上下文和证据覆盖度，并让用户查看每一项分值所对应的来源、字段、数量、时间与计算语义。

## 输出结构

| 输出 | 范围 | 是否降低主分 | 含义 |
|---|---:|---:|---|
| 公开滥用风险 / 主分 | `R=0–85`，主分 `P=100−R` | 是 | 由已覆盖的受损、攻击历史、公开滥用、扫描/爬虫与严格限权的无类别风险信号组成。 |
| 网络透明度 | `A=0–100` | 否 | Tor、代理、VPN、中继、住宅代理等当前路径属性。 |
| 网络上下文 | 标签与来源明细 | 否 | 运营商、ASN、CIDR、连接类型、IDC/托管等背景信息。 |
| 证据覆盖度 | `C=0–100` | 否 | 关键来源是否可用、字段完整及新鲜程度。 |

## 数据源与字段处理

| 来源 | 请求条件 | 用于公开滥用风险 | 用于透明度/上下文 | 关键限制 |
|---|---|---|---|---|
| ProxyCheck | 无 Key 的公开端点，仍可能受配额影响 | `compromised`、攻击历史、`scraper`、低权重 `risk` | `proxy`、`vpn`、`tor`、`hosting`、`anonymous` | 无类别 `risk` 限权，不与其他供应商总分线性叠加。 |
| Tor Project | 公开端点 | 否 | 官方 Tor 出口确认 | 仅说明当前 Tor 网络属性。 |
| AbuseIPDB | 用户配置本地 Key 后调用 | `abuseConfidenceScore`、`totalReports`、`numDistinctUsers`、`lastReportedAt` | `isTor` 仅透明度 | 独立报告者优先；同源额外报告仅按 25% 弱化计入，记录、报告者和时效需保留并连续映射。 |
| ipapi.is | 用户配置本地 Key 后调用 | `is_abuser`、`is_crawler` | `is_proxy`、`is_vpn`、`is_tor`、`is_datacenter`、`egress_service` | 数据中心和受管理出口不能单独判恶意。 |
| MaxMind GeoIP Insights | 本地 Account ID + License Key，HTTPS Basic Auth | 否 | `anonymizer`、`traits.network`、ASN、ISP、连接类型 | 仅补充匿名化和网络身份，不能作为历史滥用证据。 |
| IPHub v2.2 | 本地 Key，`X-Key` + `Accept-Version: 2.2` | 否 | `block==1`、`proxyType`、ASN、ISP、国家 | `block==2` 为可能误报的低置信提示，不能计入风险。 |
| ipify / ipapi.co / ipwho.is | 无 Key | 否 | 当前出口与地理交叉核验 | 地理或 v4/v6 差异不进入主分。 |

## 连续计算与去重

每个可计入主分的证据必须携带行为家族、来源谱系、计数、事件时间、字段粒度和来源质量先验。单条证据使用计数饱和与时间半衰期连续缩放：

```text
V(n; τ) = 1 − exp(−n / τ)
T(d; h) = 2^(−d / h)
E(g,s)  = M_g × Q_s × V(n; τ_g) × T(d; h_g) × L
```

相同来源谱系且相同行为家族仅保留最强证据。不同供应商并不自动统计独立；未被审计确认独立的数据源不得追加交叉加分。行为家族以饱和式组合，不以线性求和放大相关结论：

```text
R = 85 × [1 − Π_g (1 − E_g)]
P = 100 − R
```

v3.2 对 AbuseIPDB 使用 `n = distinctUsers + floor(0.25 × max(totalReports − distinctUsers, 0))`，并使用 `confidence^1.3` 抑制低置信度伪精度；ProxyCheck 无类别 `risk` 在同源具体行为已命中时不再重复计权。当前参数是有公开字段依据、可回放的产品先验，不是供应商正确率或 IP 恶意概率。没有跨服务商、跨时间且独立的真实标签时，不能声称评分是普适最优、最真实或概率。各参数、家族预算、时间半衰期与质量先验见 [`purity_scoring_model_v3_1.md`](purity_scoring_model_v3_1.md)。

## 字段缺失与来源状态

| 来源/字段状态 | 主分处理 | 透明度/上下文 | 覆盖度处理 | 页面状态 |
|---|---|---|---|---|
| 明确 `false` / `0` | 对应证据强度为零 | 显示“未检出” | 该字段视为已覆盖 | 未检出 |
| 明确 `true` / 正数 | 按家族规则连续计算 | 显示来源和字段 | 该字段视为已覆盖 | 命中提示 |
| 无记录 | 不产生风险 | 可显示“无记录” | 只表示请求成功，不可当作安全 | 已响应/无记录 |
| 字段缺失、类型错误或响应不完整 | 不扣风险 | 显示字段未覆盖 | 对应通道覆盖降低 | 字段未覆盖 |
| Key 未配置、429、权限不足、超时或请求失败 | 不扣风险 | 显示来源状态 | 对应通道覆盖降低 | 未配置/未覆盖 |

## 展示与交互

主卡必须显示主分、公开滥用风险、独立网络透明度和覆盖度；每一个风险桶必须描述计算来源和是否计入主分。覆盖度小于 60 时，优先显示“证据不足，未发现不等于无风险”。

授权设置中的每项 Key 和 MaxMind Account ID 均为密码样式字段，右侧提供本地“显示/隐藏”图标。所有凭据均通过 Android Keystore AES-GCM 加密存储；不得写入 URL、日志、Git、崩溃报告或项目服务器。自定义端点仅可存储，除非之后获得用户确认并基于公开文档实现专用适配器。

BrowserLeaks 仅以外部网页入口提供浏览器环境自检。EdgeOne MyIP 在本次核验中不可解析，NSTool 没有公开可审计 IP API；不得将两者伪装为默认接口、风险来源或浏览器检测结果。

## 校准边界

若未来拥有合法、最小化、定义明确且独立于供应商结论的真实业务标签，可采用按时间滚动的训练/验证切分，并以独立校准集报告 PR-AUC、Precision@K、误报率、漏报率、Brier score 及校准曲线。完成前，本模块不得把任意分数命名为发生概率。

## 参考

[1] [MaxMind GeoIP Web Services Requests](https://dev.maxmind.com/geoip/docs/web-services/requests/)

[2] [MaxMind GeoIP Web Services Responses](https://dev.maxmind.com/geoip/docs/web-services/responses/)

[3] [IPHub API v2.2](https://iphub.info/api)

[4] [AbuseIPDB API Documentation](https://docs.abuseipdb.com/)

[5] [ProxyCheck API Documentation](https://proxycheck.io/api/)

[6] [Tor Project Check API](https://check.torproject.org/api/ip)
