# MaxMind 与 IPHub 集成核验

核验时间：2026-08-27（GMT+8）

## MaxMind GeoIP Web Services

官方请求文档确认：GeoIP 的 Country、City Plus、Insights 服务均为 HTTPS GET；IP 作为路径参数。认证使用 HTTP Basic Auth，用户名为 MaxMind account ID，密码为 license key；服务要求 TLS 1.2 以上。Insights 的公开端点为：

`https://geoip.maxmind.com/geoip/v2.1/insights/{ip_address}`

集成结论：MaxMind 应作为用户配置 account ID 与 license key 后的可选 GeoIP/网络身份补充源。应用必须将两个值分别以 Android Keystore 加密保存；请求中使用 `Authorization: Basic Base64(accountId:licenseKey)`，不在 URL、日志或界面明文泄露。Insights 返回的网络/匿名化字段应进入“网络透明度/上下文”与覆盖度，不能在没有直接滥用证据时自动证明历史恶意。

## IPHub v2.2

官方文档确认：单 IP 查询使用 `GET https://v2.api.iphub.info/ip/{ip}`，需要 `X-Key` 和 `Accept-Version: 2.2` 请求头。`block=0` 表示住宅或企业 IP；`block=1` 表示非住宅（托管、代理等）；`block=2` 是低置信的可疑 IP，可能误标正常用户。`proxyType` 提供 proxy、tor、hosting、relay 和（专业版）residentialProxy 等上下文。官方建议在阻断场景仅以 `block==1` 作为默认风险等级，而不是直接阻断 `block==2`，并强调仅在敏感页面查询、避免全局拒绝。

集成结论：IPHub 是匿名化/托管网络与上下文来源，不是“历史恶意行为”来源。应用中：`block==1` 和 `proxyType` 作为透明度信号；`block==2` 仅显示“可疑/低置信”，不进入风险加分；所有 IPHub 信息仅在用户本地配置 Key 后按需查询；429、5xx、超时和字段缺失降低覆盖度而不降低风险。

## 参考

- https://dev.maxmind.com/geoip/docs/web-services/requests/
- https://iphub.info/api
