# 授权风险数据源接入说明

**修订日期：** 2026-08-18

## 接入目标

MyIPCheck 的增强纯净度诊断允许用户在自己的 Android 设备上填入 AbuseIPDB API Key、**ipapi.is API Key**，或成对保存自定义 HTTPS 请求地址与 Key。用户可通过 APP 顶部地球图标右侧的钥匙图标进入配置。应用只查询当前公网 IPv4，不读取账号、Cookie、浏览记录、设备指纹或其他本地隐私数据；配置不会提交到仓库、不写入日志、不上传至项目服务器。

> 更正：此前文档和 v1.0.5 错误写成 `ipapi.com`。正确的数据源为用户指定的 `ipapi.is`，其官方端点是 `https://api.ipapi.is/`。

## AbuseIPDB

采用 APIv2 `GET https://api.abuseipdb.com/api/v2/check`，以 `Key` HTTP Header 传递用户 Key，并提供 `ipAddress` 与 `maxAgeInDays=90` 参数。应用只解析 `abuseConfidenceScore`、`totalReports`、`isTor`、`usageType` 与 `lastReportedAt`，不请求 `verbose`，因此不会拉取报告详情或评论文本。

v2.1 使用 AbuseIPDB 原始分、报告数量与最近报告时间的连续映射，并在“滥用与攻击”风险桶内封顶；`isTor` 只作为跨来源去重的 Tor 证据。接口失败、Key 未配置、请求超时或返回未知字段时，展示“未覆盖”且不扣分。完整公式见 [`purity_scoring_model_v2.md`](purity_scoring_model_v2.md)。

## ipapi.is

采用官方 JSON POST 认证：`POST https://api.ipapi.is`，请求体为 `{"q":"{当前公网 IP}","key":"{用户 Key}"}`。这避免了将 Key 放入 URL 查询参数。服务可能用 HTTP 200 返回包含顶层 `error` 字段的错误结果，因此应用先检测 `error`，再解析结果。

应用只读取 ipapi.is 的最小安全与网络属性字段：`is_datacenter`、`is_proxy`、`is_vpn`、`is_tor`、`is_abuser`、`is_crawler`、`egress_service`、`company_name`、`asn_org`。这些明确布尔字段作为有限事实证据进入对应风险桶；`egress_service` 仅展示，不自动扣分。未配置 Key、接口超时、配额不足或未知字段不扣分。

## 自定义 HTTPS 请求地址与 Key

自定义服务由两个输入组成：完整 HTTPS 请求地址与自定义 API Key。APP 要求二者**同时填写或同时清空**，请求地址必须包含 `https://` 协议和主机名。两个值均通过与内置 Key 相同的本地加密机制保存。

当前版本不猜测自定义服务的 HTTP 方法、认证 Header、查询参数、响应 JSON 结构或风险分含义，因此**不会向自定义地址自动发送请求，也不会让它参与评分**。这样可避免在用户未定义接口格式时意外发送 Key、当前 IP 或其他数据。后续如需启用某一服务，应依据其公开 API 文档明确配置请求方法、认证位置、最小字段和独立评分映射。

## 存储与迁移策略

所有 Key 与自定义 HTTPS 地址写入私有 SharedPreferences 前，均使用 Android Keystore 生成的 AES-GCM 密钥加密。应用禁用 Android 云备份与设备迁移，并排除偏好设置、数据库和文件导出，以降低配置被备份或迁移的风险。用户可以在顶部钥匙设置中点击“清除全部”，本地密文会立即删除。

## 参考

[1] [AbuseIPDB APIv2 Documentation](https://docs.abuseipdb.com/)

[2] [ipapi.is Developers Documentation](https://ipapi.is/developers.html)
