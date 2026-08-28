# 公开 IP 信誉评分审计记录（2026-08-28）

## 权威来源结论

AbuseIPDB FAQ 将 Confidence of Abuse 定义为基于用户报告、表示某 IP 完全恶意的信心评级；其计算考虑报告数量与报告年龄，报告权重随时间衰减，并强调单个报告者不应压倒整体评级。因此它可以作为公开滥用证据输入，但不能未经校准直接当作未来恶意概率或“纯净度真值”。来源：[AbuseIPDB FAQ](https://www.abuseipdb.com/faq.html)。

AbuseIPDB API 文档说明 `abuseConfidenceScore`、`totalReports`、`numDistinctUsers` 与 `lastReportedAt` 的字段语义，并指出 `isWhitelisted` 不应单独作为行动依据；因此模型应优先使用明确行为证据、报告独立性、时间和来源覆盖，而不是把白名单、ISP、usageType 或单一二值字段当成安全证明。来源：[AbuseIPDB APIv2](https://docs.abuseipdb.com/)。

Spamhaus 公开说明其 IP reputation 使用 SIGINT、OSINT、机器学习、启发式和人工调查，并强调需要多样化数据点，涉及服务商、IP 邻域、基础设施、首次发现时间和使用方式。这证明“多源、多时间、行为与上下文分离”的方向合理，但其私有方法不可从公开页面精确复刻，也不能把托管/邻域属性直接当作恶意。来源：[Spamhaus IP reputation](https://www.spamhaus.org/ip-reputation/)。

MISP 的 Decaying Indicators of Compromise 研究支持 IoC 随时间衰减，并利用指标元数据处理异构威胁情报；但它并不提供一个适合所有 IP 的普适最终权重。来源：[Decaying Indicators of Compromise](https://arxiv.org/abs/1803.11052)。

## 当前模型审计结论

当前 v3.1 的优点是把滥用风险、匿名化透明度、基础设施上下文和证据覆盖度分开，拒绝将 VPN、Tor、IDC、ASN、国家和无记录结果直接当成恶意证据；这比把所有标签相加为“纯净度”更科学。当前主要可改进点是：在多源同一行为家族合并时进一步降低相关性重复计分；对移动/动态地址、共享 NAT、云出口和托管邻域增加“身份变动/共享性”警告而不加风险分；把 0–100 显示分明确命名为公开信号指数；在没有真实时间外标签时不宣称最优、真实或概率；对字段缺失、来源错误、过期和无记录保持覆盖度惩罚而不奖励。
