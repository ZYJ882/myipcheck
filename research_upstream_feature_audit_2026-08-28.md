# 上游公开功能核对记录（2026-08-28）

本轮被动阅读了 MyIP GitHub 仓库和 IPCheck.ing Tools 页面，作为 MyIPCheck “一次性补齐可实现能力”批次的范围基线。

## 已确认的公开工具入口

IPCheck.ing Tools 页面列出：IP Lookup、Query Any IP、Connectivity Test、WebRTC Leak Test、DNS Leak Test、Speed Test、Global Ping Test、Global MTR Test、Proxy Rule Test、DNS Resolver、Censorship Check、Whois Lookup、MAC Vendor Lookup、Browser Info、Online Security Checklist、Invisibility Test、Service Status、In-depth DNS Leak Test 和 In-depth Persona Check。

其中 WebRTC、Browser Info、In-depth Persona、深度 DNS Leak 依赖浏览器环境或远端视角；Global MTR、Censorship Check 和在线报告语义依赖全球探针或后端，Android 原生不应伪装成同等本机检测。

## 上游仓库新增线索

MyIP GitHub 主仓库当前公开目录显示 `common`、`frontend`、`api`、`public`、`scripts`、`tests` 等区域。页面的近期提交信息显示其持续扩展国际化 locale、DNS resolver health checks、Connectivity productivity import list，以及 Earth Online / outage feed 的后端门控。对于 MyIPCheck，本轮应优先吸收不依赖私有后端的用户体验和数据模型；DNS resolver health 和大型生产力清单可作为本地有限实现的候选，但必须保留请求配额、失败状态和来源说明。

## 对当前任务的执行约束

后续实现应保持以下边界：不读取 Android 账号、Cookie、浏览记录、设备 MAC 或位置；不把安全清单自评当作自动审计；不把多解析器差异当 DNS 泄漏；不把官方状态页当本机端到端可达性；不把 RIS 邻居当商业上下游；不将未授权服务商 API 作为默认源；不在 URL、日志或 Git 中暴露 Key。

## 来源

1. https://github.com/jason5ng32/MyIP
2. https://ipcheck.ing/tools/

## Globalping MTR 端点烟雾测试

2026-08-28 对 `https://api.globalping.io/v1/measurements` 以单个 US 探针、目标 `1.1.1.1`、`type=mtr`、`measurementOptions={port:80,protocol:ICMP}` 发起一次低频测量。创建返回测量编号，随后 GET 轮询一次返回 `status=finished`，结果包含 `results` 和 `rawOutput` 字段。该测试只验证端点结构，不作为应用评分或本机网络结论。
