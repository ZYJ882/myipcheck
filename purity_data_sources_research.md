# 增强纯净度诊断的数据来源与使用边界

**调研日期：** 2026-08-17

## 目标与约束

本模块只查询用户当前公网出口，不采集账号、Cookie、浏览记录、设备指纹、通讯录、定位权限或其他个人敏感信息；不抓取、逆向、绕过或使用 Ping0 的未授权 API、数据集或服务器。模块仅呈现每个数据源允许在产品中展示的少量相关结论，不镜像或转售任何供应商的完整原始响应。

## Ping0 公开能力与不可复刻项

Ping0 的 FAQ 公开说明其风控值综合扫描、爆破、爬虫、攻击、垃圾邮件和 C&C 等行为频率；其实际家庭宽带 / IDC 类型还使用人工维护的 IP 段标注，并提供共享人数与 ASN / 企业 / 注册地历史。因此这些能力不可通过单次公开查询等价复刻，也不会被标示为“Ping0 风控值”。

## 可接入的独立公开信号

| 数据源 | 拟取用字段 | 使用方式与边界 |
| --- | --- | --- |
| `api.ipify.org` | 当前 IPv4 | 仅用于确定当前出口地址。 |
| `ipapi.co` / `ipwho.is` | 国家、ASN、组织 | 多源出口、地理与公开网络属性交叉验证。 |
| `proxycheck.io` | proxy、vpn、tor、hosting、compromised、scraper、risk、confidence | 每次诊断仅针对当前 IP 请求；APP 只展示少量风险结论与来源名，不展示完整响应。其条款允许用数据支持产品向用户展示相关信息，但不允许提供完整数据或转售。 |
| `check.torproject.org/api/ip` | 当前出口是否为 Tor | 以 Tor Project 官方当前出口结果交叉验证。 |

## 增强评分规则

基础分为 100。接口失败或未覆盖均不扣分，只标记“未覆盖”。

| 信号 | 扣分规则 |
| --- | --- |
| 多源 IPv4 出口不一致 | -35 |
| IPv4 国家不一致 | -15 |
| IPv4 / IPv6 国家不一致 | -15 |
| 公开组织命中云 / 托管关键词 | -8 |
| ProxyCheck 风险 | 25–49：-8；50–74：-16；75–100：-25 |
| ProxyCheck Proxy 标记 | -18 |
| ProxyCheck VPN 标记 | -12 |
| ProxyCheck Tor 标记 | -30 |
| ProxyCheck compromised 标记 | -18 |
| ProxyCheck scraper 标记 | -10 |
| ProxyCheck hosting 标记 | -6 |
| Tor Project 当前出口标记 | -30；与 ProxyCheck Tor 同时命中时只计一次 |

分数用于呈现“独立风险与一致性提示”，不是任何第三方供应商的官方风险分或 IP 信誉承诺。新增的外部查询应在卡片中列明来源、查询时刻和隐私说明；用户可选择在没有外部风险源结果时仅查看基础一致性诊断。

## 参考

[1] https://ping0.cc/ip/faq

[2] https://proxycheck.io/

[3] https://proxycheck.io/terms

[4] https://check.torproject.org/api/ip
