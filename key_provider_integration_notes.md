# 授权风险数据源接入说明

**调研日期：** 2026-08-17

## 接入目标

MyIPCheck 的增强纯净度诊断在已有公开无 Key 数据源基础上，允许用户在自己的 Android 设备上填入 AbuseIPDB API Key 与 IPAPI Access Key。应用只查询当前公网 IPv4，不读取账号、Cookie、浏览记录、设备指纹或其他本地隐私数据；Key 不提交到仓库、不写入日志、不上传至项目服务器。

## AbuseIPDB

采用 APIv2 `GET https://api.abuseipdb.com/api/v2/check`，以 `Key` HTTP Header 传递用户 Key，并提供 `ipAddress` 与 `maxAgeInDays=90` 参数。应用只解析 `abuseConfidenceScore`、`totalReports`、`isTor`、`usageType` 与 `lastReportedAt`，不请求 `verbose`，因此不会拉取报告详情或评论文本。

评分保持当前项目的分段规则：分数 25–49 扣 8 分，50–74 扣 16 分，75–100 扣 25 分；若 `isTor=true` 且尚无其他 Tor 来源计分，再扣 30 分。接口失败、Key 未配置、请求超时或返回未知字段时，展示“未覆盖”且不扣分。

## IPAPI

采用 IPAPI Origin / IP Lookup 的 `https://api.ipapi.com/api/{ip}?access_key=...&security=1` 请求。应用只解析 `security` 对象中的 `is_proxy`、`is_crawler`、`is_tor`、`is_anonymous`、`is_cloud_provider`、`threat_level` 与 `threat_types`。该服务要求将 Access Key 放在 URL 查询参数；因此 APP 不打印请求 URL，也不在错误提示、日志或报告中显示 Key。

评分沿用公开风险源的可解释规则：`threat_level` 为 low / medium / high 时分别扣 8 / 16 / 25 分；proxy、crawler、cloud provider 与尚未被其他来源计分的 Tor 标记分别按 18、10、6、30 分处理。未知、Key 未配置或接口不可用均不扣分。

## 自定义预留 Key

第三个“自定义预留 Key”字段仅用于未来由用户指定、具有明确 API 文档与授权条件的数据源。当前版本将其通过同一加密机制保存，但**不会发送给任何网络端点，也不会参与评分**。

## 存储与迁移策略

三类 Key 写入私有 SharedPreferences 前使用 Android Keystore 生成的 AES-GCM 密钥加密。应用禁用 Android 云备份与设备迁移，并排除偏好设置、数据库和文件导出，以降低 Key 被备份或迁移的风险。用户可在设置卡片中“清除全部”，本地密文将被立即删除。

## 参考

[1] https://docs.abuseipdb.com/

[2] https://ipapi.com/documentation

[3] https://docs.apilayer.com/ipapi/docs/api-documentation
