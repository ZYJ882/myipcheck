# 增强透明纯净度诊断模块规格

## 产品定位

模块参考 Ping0 公开展示的风险、IP 类型、原生性、共享与稳定性思路，但不会复制或伪称其专有人工标注、服务器或风险分。Android APP 显示的名称为 **增强透明纯净度诊断**，用于说明当前公网出口可公开验证的一致性、代理 / Tor / 托管属性与独立风险提示，而不是 Ping0 风控数据库或业务反欺诈结论。

## 评分模型

初始分数为 100 分。评分仅在公开数据源返回明确异常或不一致时扣减；接口超时、未知值、未覆盖字段或 Android 无法读取的数据必须显示为“未覆盖”而非扣分。相同类别的 Tor 结论只扣一次，避免对同一证据重复计分。

| 可验证信号 | 规则 | 调整 |
| --- | --- | --- |
| 多源出口一致性 | `api.ipify.org` 与 `ipwho.is` 返回的 IPv4 不一致 | -35 |
| 多源国家一致性 | `ipapi.co` 与 `ipwho.is` 返回的 IPv4 国家代码不一致 | -15 |
| 双栈地理一致性 | 同时可获得 IPv4、IPv6 且国家代码不一致 | -15 |
| 托管网络提示 | 公开 ASN / ISP / 组织字段命中云、托管、数据中心关键词 | -8 |
| 外部风险分 | `proxycheck.io` 结果：25–49 / 50–74 / 75–100 | -8 / -16 / -25 |
| Proxy 标记 | `proxycheck.io` 明确标记代理 | -18 |
| VPN 标记 | `proxycheck.io` 明确标记 VPN | -12 |
| Tor 标记 | `proxycheck.io` 或 Tor Project 官方接口明确标记 Tor | -30，仅计一次 |
| Compromised 标记 | `proxycheck.io` 明确标记受损 / 恶意活动 | -18 |
| Scraper 标记 | `proxycheck.io` 明确标记爬虫 | -10 |
| Hosting 标记 | `proxycheck.io` 明确标记托管网络 | -6 |
| AbuseIPDB 置信度 | 用户配置 Key 后，`abuseConfidenceScore` 为 25–49 / 50–74 / 75–100 | -8 / -16 / -25 |
| AbuseIPDB Tor | 用户配置 Key 后，`isTor=true` | -30；若已由其他 Tor 来源计分则不重复扣分 |
| ipapi.is 托管标记 | 用户配置 Key 后，顶层 `is_datacenter=true` | -6 |
| ipapi.is 代理 / VPN | 用户配置 Key 后，顶层 `is_proxy=true` / `is_vpn=true` | -18 / -12 |
| ipapi.is Tor | 用户配置 Key 后，顶层 `is_tor=true` | -30；若已由其他 Tor 来源计分则不重复扣分 |
| ipapi.is 滥用 / 爬虫 | 用户配置 Key 后，顶层 `is_abuser=true` / `is_crawler=true` | -18 / -10 |
| ipapi.is 受管理出口 | 用户配置 Key 后，顶层 `egress_service` 存在 | 0，仅展示 |
| Android VPN / Private DNS | 系统显示 VPN 或 Private DNS | 0，仅展示网络状态 |

评分档位为：90–100“出口一致”、70–89“轻度提示”、40–69“存在明显风险或不一致”、0–39“高风险提示”。该档位为独立模型的结果，不使用“Ping0 风控值”“极度纯净”“原生 IP”或“家庭宽带 / IDC”作为结论。

## 证据展示

报告应分组显示：出口与位置一致性、公开网络属性、代理 / VPN / Tor、攻击与风险提示、授权数据源证据和 Android 网络状态。对于外部风险数据，仅显示与用户当前结果直接相关的少量结论、来源和查询时刻，不显示或转售供应商完整响应。

## 数据与隐私

模块按需调用 `api.ipify.org`、`ipapi.co`、`ipwho.is`、`proxycheck.io` 和 `check.torproject.org`。当用户自行配置 Key 后，模块还会调用 AbuseIPDB APIv2 Check 与 ipapi.is 官方 JSON POST 端点。调用仅使用当前公网 IP 或由服务端自动识别当前来源 IP，不携带账号、Cookie、浏览记录、设备指纹、位置权限或其他个人敏感数据。三类 Key 使用 Android Keystore 的 AES-GCM 密钥加密保存在私有本地偏好设置，并禁用系统备份和设备迁移；自定义预留 Key 当前不会发往任何服务。应用不在本地持久化保存 IP 历史或检测报告；结果仅保留在当前内存界面。第三方服务可能按照各自隐私政策记录请求日志，用户可不运行该模块而继续使用其他 APP 功能。

## 不覆盖的 Ping0 专有能力

模块不覆盖或伪造：人工 IP 段类型标注、Ping0 自有恶意行为信誉、共享人数估算、BGP / ASN / 企业 / 注册地历史、RIR 注册地原生性判定、CDN 全网扫描标记、AI 商业宽带识别及基于全球探针的精确定位。需要这些能力时，应使用对应服务的官方页面或具有明确授权的数据接口。

## 参考

[1] https://ping0.cc/ip/faq

[2] https://proxycheck.io/terms

[3] https://check.torproject.org/api/ip

[4] https://docs.abuseipdb.com/

[5] https://ipapi.is/developers.html
