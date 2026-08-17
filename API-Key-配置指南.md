# API Key 配置指南

## 用途

从 v1.0.6 起，NetScope 的“增强纯净度诊断”允许你在**自己的手机**中配置 AbuseIPDB 和 ipapi.is 的授权 Key。配置后，APP 会在原有公开信号基础上增加授权的恶意 IP 信誉与 security 风险证据；评分仍采用“有明确证据才扣分，未配置、超时或未知不扣分”的原则。

> 请不要把 Key 发到聊天、Issues、截图、README、源码压缩包或 GitHub Secrets。Key 应只填入已安装 APP 的设置卡片。

## 应用内操作

打开 APP 首页，向下滑到 **增强纯净度诊断** 后的 **授权数据源 Key** 卡片。将 Key 粘贴到对应的密码输入框后，点击 **加密保存并检测**。保存成功后 APP 会立即重新执行诊断。

| 输入框 | 应填写的内容 | 会请求的官方端点 | 纳入报告的字段 |
| --- | --- | --- | --- |
| AbuseIPDB API Key | 来自 AbuseIPDB 账户 API 页面的一串 API Key | `api.abuseipdb.com/api/v2/check` | `abuseConfidenceScore`、报告数量、`isTor`、使用类型与最近报告时间。 |
| ipapi.is API Key | 来自 **ipapi.is** 控制台的 API Key；不是 `ipapi.co` 或 `ipapi.com` 的 Key | `POST api.ipapi.is`，JSON 请求体包含当前 IP 与 Key | 顶层 `is_datacenter`、`is_proxy`、`is_vpn`、`is_tor`、`is_abuser`、`is_crawler` 与 `egress_service`。 |
| 自定义预留 Key | 未来其他具有明确 API 文档与授权条件的数据源 Key | 当前不会发送 | 仅在手机本地加密保存，不参与本版评分。 |

## 评分规则

AbuseIPDB 的 `abuseConfidenceScore` 为 25–49、50–74、75–100 时，分别扣 8、16、25 分。ipapi.is 的 `is_datacenter`、`is_proxy`、`is_vpn`、`is_tor`、`is_abuser`、`is_crawler` 标记分别按 6、18、12、30、18、10 分处理；`egress_service` 仅说明，不自动扣分。Tor 证据会跨所有数据源去重，避免同一结论被反复扣分。

Key 未配置、请求超时、配额耗尽、权限不足或服务返回未知字段时，页面显示 **未配置** 或 **未覆盖**，不会扣分。该分数是独立风险提示，不是 Ping0 风控值，也不可作为账号、支付、广告、访问控制或合规决定的唯一依据。

## 本地安全与隐私

Key 在存入私有应用设置前，使用 Android Keystore 管理的 AES-GCM 密钥加密。应用禁用了系统云备份和设备迁移，并排除了偏好设置、数据库和文件的导出；你可以随时点击 **清除全部** 删除三个 Key 的本地密文。

诊断只会将当前公网 IP 发往你所配置的对应服务；ipapi.is 使用官方 JSON POST 认证，因此 Key 不会被放入 URL，不读取账号、密码、Cookie、浏览记录、设备指纹、通讯录或位置权限。第三方服务可能依照各自隐私政策记录请求日志，请在使用前确认你自己的账户套餐、配额与服务条款。

## 参考

[1] https://docs.abuseipdb.com/

[2] https://ipapi.is/developers.html
