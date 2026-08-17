# ipapi.is 接入修正调研

**调研日期：** 2026-08-17

## 更正结论

用户指定的数据源是 **ipapi.is**（`https://api.ipapi.is/`），不是此前误接入的 `ipapi.com`。此前代码中 `https://api.ipapi.com/api/{ip}?access_key=...&security=1` 及 `security` 嵌套对象解析必须移除。

## 官方请求方式

ipapi.is 官方文档规定：

| 项目 | 正确方式 |
| --- | --- |
| 标准查询 | `GET https://api.ipapi.is/?q={ip}` |
| API Key 认证 | `key` 查询参数或 JSON POST Body。 |
| 推荐安全方式 | `POST https://api.ipapi.is`，请求 JSON 为 `{"q":"{ip}","key":"{key}"}`，避免 Key 出现在 URL。 |
| 未配置 Key | 可匿名查询，每个客户端 IP 每 UTC 日最多 100 次，返回最小字段。 |
| 配置 Key | 返回完整响应与更高配额，具体取决于套餐。 |
| 错误 | 响应可使用 HTTP 200，但顶层存在 `error` 字段，必须主动检查。 |

## 评分可用字段

ipapi.is 将安全字段置于顶层，而不是 `security` 对象。应用仅解析以下最小字段：`is_datacenter`、`is_tor`、`is_proxy`、`is_vpn`、`is_abuser`、`is_crawler`、`egress_service`、`company_name`、`asn_num`、`asn_org`。文档还说明 `is_datacenter` 用于托管网络识别，`is_tor`、`is_proxy`、`is_vpn` 与 `is_abuser` 是风险相关标记。

修订后的规则沿用原本“有明确信号才扣分、未知不扣分”的方法：datacenter -6，proxy -18，VPN -12，Tor -30（跨数据源只计一次），abuser -18，crawler -10，受管理出口服务只说明不自动扣分。ipapi.is 没有被确认可用的统一 `threat_level` 字段，因此该错误评分项应删除。

## AbuseIPDB 复核

AbuseIPDB 仍使用官方 APIv2 Check 端点：`GET https://api.abuseipdb.com/api/v2/check?ipAddress={ip}&maxAgeInDays=90`，Key 放在 `Key` HTTP Header。应用只读取 `abuseConfidenceScore`、`totalReports`、`isTor`、`usageType` 和 `lastReportedAt`，不使用 `verbose`，不下载报告评论。

## 参考

[1] https://ipapi.is/developers.html

[2] https://api.ipapi.is/?q=8.8.8.8

[3] https://docs.abuseipdb.com/
