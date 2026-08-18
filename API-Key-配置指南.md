# API Key 配置指南

## 用途

从 v1.0.10 起，NetScope 的“增强纯净度诊断”允许你在**自己的手机**中配置 AbuseIPDB、ipapi.is 与自定义数据源的授权信息。AbuseIPDB 和 ipapi.is 会在原有公开信号基础上增加授权的 IP 信誉与 security 证据；自定义数据源当前只保存配置，不会自动发起请求或参与评分。

> 请不要把 Key 发到聊天、Issues、截图、README、源码压缩包或 GitHub Secrets。Key 与自定义请求地址只应填入已安装 APP 的本地设置入口。

## 应用内操作

打开 APP 首页，点击**顶部地球图标右侧的钥匙图标**，即可打开“授权数据源 Key”设置。填写后点击 **加密保存**；内置来源保存成功后，APP 会重新执行纯净度诊断。

| 配置项 | 应填写的内容 | 当前行为 |
| --- | --- | --- |
| AbuseIPDB API Key | 来自 AbuseIPDB 账户 API 页面的一串 API Key | 请求 `api.abuseipdb.com/api/v2/check`，用于 `abuseConfidenceScore`、报告数量、`isTor`、使用类型与最近报告时间。 |
| ipapi.is API Key | 来自 **ipapi.is** 控制台的 API Key；不是 `ipapi.co` 或 `ipapi.com` 的 Key | 通过 `POST https://api.ipapi.is` 查询当前 IP，使用 `is_datacenter`、`is_proxy`、`is_vpn`、`is_tor`、`is_abuser`、`is_crawler` 与 `egress_service`。 |
| 自定义请求地址 | 完整 HTTPS URL，例如 `https://api.example.com/v1/check` | 与自定义 Key 成对加密保存在本机；当前版本不自动向该地址发送请求。 |
| 自定义 API Key | 具有明确 API 文档和授权条件的第三方服务 Key | 必须与自定义 HTTPS 请求地址同时填写或同时清空；当前版本只保存，不参与评分。 |

自定义地址必须使用 HTTPS 且包含主机名。为避免无意调用未知接口、泄露 Key 或误解响应格式，APP 不会猜测认证方式、请求方法、参数结构或评分含义；需要启用自定义服务时，应根据该服务的公开 API 文档另行定义请求与解析规则。

## 评分规则与未覆盖原则

AbuseIPDB 的原始分、报告数量和时效，及 ProxyCheck 的风险、置信度、最近检出时间和攻击历史，均以连续公式映射为一位小数的风险值。ipapi.is 与 Tor Project 等仅返回“是 / 否”的字段会作为明确但有限的事实证据处理；同类证据在风险桶内去重并受上限约束。完整公式请见 [`purity_scoring_model_v2.md`](purity_scoring_model_v2.md)。

Key 未配置、请求超时、配额耗尽、权限不足或服务返回未知字段时，页面显示 **未配置** 或 **未覆盖**，不会扣分。该分数是独立风险提示，不是 Ping0 风控值，也不可作为账号、支付、广告、访问控制或合规决定的唯一依据。

## 本地安全与隐私

Key 与自定义 HTTPS 请求地址在存入私有应用设置前，均使用 Android Keystore 管理的 AES-GCM 密钥加密。应用禁用了系统云备份和设备迁移，并排除了偏好设置、数据库和文件的导出；你可以在顶部钥匙设置中点击 **清除全部**，删除所有本地密文。

诊断只会将当前公网 IP 发往内置且已配置的 AbuseIPDB 与 ipapi.is 服务。自定义地址和 Key 在本版本**不会被发送**。APP 不读取账号、密码、Cookie、浏览记录、设备指纹、通讯录或位置权限。第三方服务可能依照各自隐私政策记录请求日志，请在使用前确认你自己的账户套餐、配额与服务条款。

## 参考

[1] [AbuseIPDB APIv2 Documentation](https://docs.abuseipdb.com/)

[2] [ipapi.is Developers Documentation](https://ipapi.is/developers.html)
