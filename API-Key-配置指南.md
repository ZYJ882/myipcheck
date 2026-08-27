# API Key 配置指南

## 用途

从 **v1.0.12** 起，NetScope 的“增强纯净度诊断”可在**用户自己的手机**中配置 AbuseIPDB、ipapi.is、MaxMind GeoIP Insights 和 IPHub 的授权信息。每个服务的端点已在应用中预置；只有填写了对应凭据后，应用才会按需查询该服务。自定义数据源仍只保存配置，不会自动请求或参与评分。

> **请不要把任何 Account ID、License Key 或 API Key 发到聊天、Issue、截图、README、源码压缩包或 GitHub Actions Secrets。** 凭据应仅填入已经安装的 APP 本地设置入口。

## 应用内操作

打开 APP 首页，点击**顶部地球图标右侧的钥匙图标**，即可打开“授权数据源 Key”设置。每个密钥输入框的右侧均有一个**小眼睛**；点击即可在当前页面显示或隐藏该字段。填写完成后选择 **加密保存**，APP 会重新执行诊断。

| 配置项 | 应填写的内容 | 预置端点与当前行为 |
| --- | --- | --- |
| AbuseIPDB API Key | AbuseIPDB 账户中的 API Key | `GET https://api.abuseipdb.com/api/v2/check`，请求头为 `Key`。可返回置信分、报告数、独立报告者、Tor、使用类型和最近报告时间。 |
| ipapi.is API Key | **ipapi.is** 控制台的 Key，不是 ipapi.co / ipapi.com 的 Key | `POST https://api.ipapi.is`。`is_abuser` 与 `is_crawler` 可作为公开行为证据；代理、VPN、Tor、数据中心和受管理出口只单列展示。 |
| MaxMind Account ID | MaxMind GeoIP Web Services 账户 ID | 必须与 License Key 成对填写，应用通过 HTTPS Basic Auth 调用 `GET https://geoip.maxmind.com/geoip/v2.1/insights/{ip}`。 |
| MaxMind License Key | 与 Account ID 对应、已开通 GeoIP Insights 的 License Key | 仅与 Account ID 配套使用。匿名化、Tor、代理、托管、网络、ASN、ISP 与连接类型只进入透明度、上下文和覆盖度。 |
| IPHub API Key | IPHub 账户的 API Key | `GET https://v2.api.iphub.info/ip/{ip}`，请求头为 `X-Key` 与 `Accept-Version: 2.2`。`block==1` 和 `proxyType` 只解释网络透明度/上下文；`block==2` 为可能误报的低置信提示。 |
| 自定义请求地址 | 完整 HTTPS URL，例如 `https://api.example.com/v1/check` | 与自定义 Key 成对加密保存在本机；当前版本不自动向该地址发送请求。 |
| 自定义 API Key | 具有明确 API 文档和授权条件的第三方服务 Key | 必须与自定义 HTTPS 请求地址同时填写或清空；当前版本只保存，不参与评分。 |

MaxMind 的 Account ID 和 License Key 必须同时填写；其中任一项为空时，保存按钮会提示补齐。所有预置请求仅使用 HTTPS；Key 与 MaxMind Account ID 不会拼入 URL。

## 网页工具与未支持来源

BrowserLeaks 是网页内 JavaScript、WebRTC、Canvas/WebGL、TLS、DNS 与指纹自检工具。应用提供其外部入口，但原生 Android 不会冒充已执行这些浏览器测试，也不把网页检测结果混入 IP 历史滥用主分。

`myip.edgeone.ai` 在本次网络核验中无法解析，NSTool 没有公开可审计的 IP 情报 API。因此，APP 不会对它们发起默认请求、不把它们计分，也不会下载它们指向的 APK。

## 评分与未覆盖原则

v3.1 的主分是**公开滥用风险信号的反向展示**。每一分由已覆盖来源中的行为家族、来源质量先验、计数饱和和时间衰减连续计算；主分不是欺诈概率、账号信誉或访问控制决定。Tor、代理、VPN、中继、IDC、托管、ASN、CIDR、ISP、地理及出口差异只显示为网络透明度或上下文，绝不凭这些网络属性单独降低主分。

Key 未配置、请求超时、429、配额耗尽、权限不足、响应字段缺失或类型错误时，页面应显示 **未配置** 或 **未覆盖**，并降低**证据覆盖度**；它们不会被静默当作无风险或直接扣分。当覆盖度低于 60 时，页面会优先提示“证据不足”。完整计算规则、参数和可回放约束见 [`purity_scoring_model_v3_1.md`](purity_scoring_model_v3_1.md)。

## 本地安全与隐私

所有 Key、MaxMind Account ID 及自定义 HTTPS 请求地址在进入私有应用设置前，均使用 Android Keystore 管理的 AES-GCM 密钥加密。应用禁用了系统云备份和设备迁移，并排除了偏好设置、数据库和文件的导出。你可以在顶部钥匙设置中点击 **清除全部**，删除所有本地密文。

诊断只会将当前公网 IP 发给无需 Key 的公开来源，以及用户自行配置且保存成功的 AbuseIPDB、ipapi.is、MaxMind 和 IPHub。自定义地址与 Key 不会被发送。APP 不读取账号密码、Cookie、浏览记录、设备指纹、通讯录或位置权限；第三方服务仍可能依照各自隐私政策记录请求日志，请在使用前确认自己的服务条款与配额。

## 参考

[1] [AbuseIPDB APIv2 Documentation](https://docs.abuseipdb.com/)

[2] [ipapi.is Developers Documentation](https://ipapi.is/developers.html)

[3] [MaxMind GeoIP Web Services Requests](https://dev.maxmind.com/geoip/docs/web-services/requests/)

[4] [MaxMind GeoIP Web Services Responses](https://dev.maxmind.com/geoip/docs/web-services/responses/)

[5] [IPHub API v2.2](https://iphub.info/api)
