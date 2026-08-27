## v1.0.12：多源 IP 情报与可回放风险评分

本版本将 MyIPCheck 的授权数据源扩展为 **AbuseIPDB、ipapi.is、MaxMind GeoIP Insights 与 IPHub v2.2**，并将展示逻辑升级为可回放的 **v3.1 公开网络出口风险模型**。更新保持“风险信号指数”定位，不把主分表述为账号信誉、欺诈概率或第三方专有风控值。

| 更新项 | 说明 |
|---|---|
| MaxMind GeoIP Insights | 预置 `https://geoip.maxmind.com/geoip/v2.1/insights/{ip}`；通过本地配置的 Account ID 与 License Key 使用 HTTPS Basic Auth。匿名化与网络字段只进入透明度、上下文和覆盖度。 |
| IPHub v2.2 | 预置 `https://v2.api.iphub.info/ip/{ip}`；通过本地 `X-Key` 和 `Accept-Version: 2.2` 调用。`block=1` 与 `proxyType` 作为透明度/上下文；`block=2` 仅显示低置信提示，不作为风险加分。 |
| Key 可见性控制 | 每个 Key 与 MaxMind Account ID 输入框均提供独立“小眼睛”，可由用户在本机即时显示/隐藏。全部凭据仍通过 Android Keystore AES-GCM 加密存储，不写入 URL、日志或仓库。 |
| v3.1 主分 | 主分仅反映公开滥用行为证据的反向展示；受损、攻击历史、已报告滥用、爬虫和严格限权的无类别供应商风险按来源质量、计数饱和、时间衰减和非线性组合连续计算。 |
| 网络透明度 | Tor、代理、VPN 和中继以 0–100 的独立透明度刻度展示；即使被识别，也不再直接降低“公开滥用风险”主分。 |
| 网络上下文 | IDC、托管、ASN、CIDR、ISP 与出口差异只作解释性信息，不用基础设施身份推断历史恶意。 |
| 覆盖度 | Key 未配置、配额耗尽、429、超时、响应失败与字段缺失降低证据覆盖度，但不会被静默解释为低风险。覆盖度低于 60 时，优先显示“证据不足”。 |
| 浏览器专属检测 | 增加 BrowserLeaks 外部自检入口；该类网页 JavaScript、WebRTC、Canvas/WebGL、TLS 和 DNS 检测不会被原生应用伪造或混入主分。 |
| 来源边界 | `myip.edgeone.ai` 在本次网络核验中无法解析，NSTool 未公开可审计 IP 情报 API；两者不会作为默认请求、评分来源或 APK 下载来源。 |
| 回归验证 | 更新 `scripts/validate_purity_model.py`，验证连续相邻分、时间衰减、透明度隔离、覆盖度缺失治理与输出范围。 |

本地调试构建使用 `versionCode 1012` 和 `versionName 1.0.12`。GitHub 发布工作流仍会在推送 `main` 后根据运行序号覆盖自动发布的版本名与版本码，并生成签名 APK 与 GitHub Release。

### 参考

- [MaxMind GeoIP Web Services Requests](https://dev.maxmind.com/geoip/docs/web-services/requests/)
- [MaxMind GeoIP Web Services Responses](https://dev.maxmind.com/geoip/docs/web-services/responses/)
- [IPHub API v2.2](https://iphub.info/api)
- [AbuseIPDB API Documentation](https://docs.abuseipdb.com/)
- [ProxyCheck API Documentation](https://proxycheck.io/api/)
