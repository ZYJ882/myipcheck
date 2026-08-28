# 高级网络诊断公开 API 边界

本文件记录 v1.0.22 高级诊断功能所使用的公开资料与实现约束。

| 功能 | 数据源与端点 | 应用中的受控用法 | 解释边界 |
|---|---|---|---|
| DNS 交叉查询 | Cloudflare `https://cloudflare-dns.com/dns-query`、Google `https://dns.google/resolve`、Quad9 `https://dns.quad9.net/dns-query`、DNS.SB `https://doh.dns.sb/dns-query?` | 用户手动输入域名和记录类型；仅支持 A、AAAA、TXT、MX、NS、CNAME | 系统解析与 DoH 答案差异可能来自 CDN、地域调度、缓存或安全策略，不能单独作为 DNS 泄漏证据。DNS.SB 失败时保留为错误/未覆盖，不影响其他解析器结果。 |
| ASN 概览/邻居 | RIPEstat `as-overview` 与 `asn-neighbours` | 查询指定 ASN，显示持有人、路由可见性、独立邻居数与最多 12 条观测邻居 | 邻居由 RIS 所见 AS 路径得出；`left`/`right` 代表路径位置，并不自动等价于商业上游/下游关系。 |
| 全球 ping / MTR | Globalping `POST /v1/measurements`、`GET /v1/measurements/{id}` | 每次固定美国、德国、新加坡各 1 个探针，ping 使用 3 个包，MTR 使用 ICMP/80 端口并最多轮询 4 次；仅用户点击后调用，MTR UI 最多展示 64 跳 | 结果是远端探针到目标的测量，不代表本机网络时延或本机路由。中间跳丢包可能只是 ICMP 限速；未注册使用者目前有 50 个探针/测量与 250 个免费测试/小时，限制可能变化。 |
| OONI 历史测量 | OONI `GET /api/v1/measurements` | 查询指定国家、域名的 Web Connectivity 元数据；固定 `limit=5`，仅手动调用 | 结果是历史志愿者测量元数据，包含异常、确认、失败和验证状态；不能证明用户当前网络是否遭审查。OONI API 不适合大规模提取并实行速率限制。 |

## 参考资料

[1] [Globalping limits and credits](https://globalping.io/credits)

[2] [Globalping: Run ping with HTTP using the API](https://blog.globalping.io/run-ping-with-http-using-globalping-api/)

[3] [OONI: Accessing OONI data](https://docs.ooni.org/data)

[4] [RIPEstat: ASN Neighbours](https://stat.ripe.net/docs/data-api/api-endpoints/asn-neighbours)

[5] [RIPEstat: AS Overview](https://stat.ripe.net/docs/02.data-api/as-overview.html)
