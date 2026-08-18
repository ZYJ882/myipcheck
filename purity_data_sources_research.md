# 增强纯净度诊断的数据来源与使用边界

**调研日期：** 2026-08-18
**当前评分版本：** v2.1（连续映射）

## 目标与约束

本模块只查询用户当前公网出口，不采集账号、Cookie、浏览记录、设备指纹、通讯录、定位权限或其他个人敏感信息；不抓取、逆向、绕过或使用 Ping0 的未授权 API、数据集或服务器。模块只呈现每个数据源允许在产品中展示的少量相关结论，不镜像、转售或公开任何供应商的完整原始响应。[1] [2]

## Ping0 公开能力与不可复刻项

Ping0 的公开说明涉及扫描、爆破、爬虫、攻击、垃圾邮件和 C&C 等行为频率，也包含人工维护的 IP 段标注、共享人数和历史 ASN / 企业 / 注册地信息。因此这些能力不能通过单次公开查询等价复刻，APP 不会将自己的分数标示为“Ping0 风控值”。[3]

## 可接入的独立公开信号

| 数据源 | 已读取字段 | v2.1 使用方式与边界 |
| --- | --- | --- |
| `api.ipify.org` | 当前 IPv4 | 用于确定当前出口，并与 `ipwho.is` 出口地址交叉验证。 |
| `ipapi.co` / `ipwho.is` | 国家、ASN、组织 | 展示地理与网络属性；国家或 IPv4 / IPv6 地理差异不扣分。 |
| `proxycheck.io` v3 | proxy、vpn、tor、hosting、compromised、scraper、risk、confidence、first_seen、last_seen、attack_history | `risk`、`confidence`、`last_seen` 和攻击事件总数进入连续公式；布尔检测只作为明确事实，不伪造不存在的概率。 |
| `check.torproject.org/api/ip` | 当前出口是否为 Tor | 官方二元确认；确认时使用匿名化桶上限。 |
| AbuseIPDB APIv2 Check | abuseConfidenceScore、totalReports、lastReportedAt、isTor、usageType | 原始分、报告量和时效进入连续公式。 |
| `api.ipapi.is` | is_datacenter、is_proxy、is_vpn、is_tor、is_abuser、is_crawler、egress_service | 提供明确布尔安全属性；受管理出口仅展示。 |

ProxyCheck 条款允许将其数据用于支持产品中向用户呈现相关信息，但不允许提供完整数据或转售；本 APP 使用用户在其设备上直接发起的查询，仅显示必要的少量结论。[2]

## v2.1 评分方法说明

此前文件中存在的 `-35 / -15 / -8` 线性扣分表和 `25–49 / 50–74 / 75–100` 固定分段已经在 **v1.0.9** 起废弃，不能用于解释当前 APP 的结果。现行模型以匿名化、滥用攻击、托管基础设施、观测完整性四个有上限的风险桶进行计算。

有 0–100 原始输入的 AbuseIPDB 与 ProxyCheck 使用连续函数，而不是阈值跳档。ProxyCheck 信号还会按其返回的置信度和最近检出时间缩放；AbuseIPDB 还会按窗口内报告数量和最近报告时间平滑调整。因为 ipapi.is、Tor Project 等字段本身只提供二元结果，它们保留公开、有限的基础权重，并在诊断明细中解释该信息限制。

> 接口失败、Key 未配置、配额耗尽、未知值和字段未覆盖均显示为“未覆盖”，不计入风险。

完整公式、逐分示例、交叉验证增量、类别上限和隐私边界见 [`purity_scoring_model_v2.md`](purity_scoring_model_v2.md) 与 [`purity_module_spec.md`](purity_module_spec.md)。

## 参考

[1] [ProxyCheck API Documentation](https://proxycheck.io/api/)

[2] [ProxyCheck Terms](https://proxycheck.io/terms)

[3] [Ping0 IP FAQ](https://ping0.cc/ip/faq)

[4] [AbuseIPDB APIv2 Documentation](https://docs.abuseipdb.com/)

[5] [ipapi.is Developers Documentation](https://ipapi.is/developers.html)

[6] [Tor Project IP API](https://check.torproject.org/api/ip)
