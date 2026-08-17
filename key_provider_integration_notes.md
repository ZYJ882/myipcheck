# 授权风险数据源接入说明

**修订日期：** 2026-08-17

## 接入目标

MyIPCheck 的增强纯净度诊断允许用户在自己的 Android 设备上填入 AbuseIPDB API Key 与 **ipapi.is API Key**。应用只查询当前公网 IPv4，不读取账号、Cookie、浏览记录、设备指纹或其他本地隐私数据；Key 不提交到仓库、不写入日志、不上传至项目服务器。

> 更正：此前文档和 v1.0.5 错误写成 `ipapi.com`。正确的数据源为用户指定的 `ipapi.is`，其官方端点是 `https://api.ipapi.is/`。

## AbuseIPDB

采用 APIv2 `GET https://api.abuseipdb.com/api/v2/check`，以 `Key` HTTP Header 传递用户 Key，并提供 `ipAddress` 与 `maxAgeInDays=90` 参数。应用只解析 `abuseConfidenceScore`、`totalReports`、`isTor`、`usageType` 与 `lastReportedAt`，不请求 `verbose`，因此不会拉取报告详情或评论文本。

评分保持当前项目的分段规则：分数 25–49 扣 8 分，50–74 扣 16 分，75–100 扣 25 分；若 `isTor=true` 且尚无其他 Tor 来源计分，再扣 30 分。接口失败、Key 未配置、请求超时或返回未知字段时，展示“未覆盖”且不扣分。

## ipapi.is

采用官方推荐的 JSON POST 认证：`POST https://api.ipapi.is`，请求体为 `{"q":"{当前公网 IP}","key":"{用户 Key}"}`。这避免了将 Key 放入 URL 查询参数。服务可能用 HTTP 200 返回包含顶层 `error` 字段的错误结果，因此应用先检测 `error`，再解析结果。

应用只读取 ipapi.is 的最小安全与网络属性字段：`is_datacenter`、`is_proxy`、`is_vpn`、`is_tor`、`is_abuser`、`is_crawler`、`egress_service`、`company_name`、`asn_org`。评分与之前的证据规则一致：托管 -6，代理 -18，VPN -12，Tor -30（跨来源去重），滥用 -18，爬虫 -10；`egress_service` 只展示，不自动扣分。未配置 Key、接口超时、配额不足或未知字段不扣分。

## 自定义预留 Key

第三个“自定义预留 Key”字段仅用于未来由用户指定、具有明确 API 文档与授权条件的数据源。当前版本将其通过同一加密机制保存，但**不会发送给任何网络端点，也不会参与评分**。

## 存储与迁移策略

三类 Key 写入私有 SharedPreferences 前使用 Android Keystore 生成的 AES-GCM 密钥加密。应用禁用 Android 云备份与设备迁移，并排除偏好设置、数据库和文件导出，以降低 Key 被备份或迁移的风险。用户可在设置卡片中“清除全部”，本地密文将被立即删除。

## 参考

[1] https://docs.abuseipdb.com/

[2] https://ipapi.is/developers.html
