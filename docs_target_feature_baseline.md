# MyIP / ipcheck.ing 功能基线

核验日期：2026-08-27（网页公开内容）

## 目标项目公开功能

GitHub 仓库 README 将 MyIP 定位为 IP Toolbox，公开列出以下能力：IP/地理信息、多探针 IPv4/IPv6、任意 IP 查询、IP 历史、浏览器指纹；WebRTC 泄漏、DNS 泄漏、安全清单；网络连通性、速度测试、全球延迟、MTR、审查/封锁检查、代理规则；DNS Resolver、Whois、MAC Lookup、ASN 信息与上游拓扑、服务状态；可分享报告、Curl API、Earth Online、深色模式、PWA、键盘快捷键和多语言。

README 还说明 Docker/Node 部署方式，以及 MaxMind GeoLite2 凭据和真实域名部署时的 ALLOWED_DOMAINS 配置要求。

## ipcheck.ing 当前首页入口

首页实际展示：IP Infos、Network Connectivity、WebRTC Leak Test、DNS Leak Test、Speed Test；Advanced Tools 下有 Global Latency Test、MTR Test、Rule Test、DNS Resolution、Censorship Check、Whois Search、MAC Lookup、Browser Information、Security Checklist、Service Status、Invisibility Test、In-depth DNS Leak Test 和 In-depth Persona Check。

IP Infos 同时展示 IPv4 与 IPv6 探针状态；连通性测试支持网站列表、重新测试、添加测试以及多站点延迟；WebRTC 测试显示多个 STUN 连接、暴露地址、NAT、ISP、地区与 SDP Log；DNS Leak Test 用多个 DNS Endpoint 判断解析地域；Speed Test 使用 Cloudflare Edge，允许选择下载/上传包大小并输出 Download、Upload、Latency、Jitter。

## 对照审计注意事项

MyIPCheck 是 Android 原生应用，不能把浏览器专属的 WebRTC、Canvas/WebGL、浏览器指纹或 PWA 能力直接等同复刻；需要区分“原生可实现”“需要后端/全球探针”“仅能提供外部网页入口”和“暂未实现”。

GitHub 仓库目录包含 frontend、api、common、public、scripts 和 tests 等大量模块，不能仅通过 README 判断具体实现；下一步应读取路由、组件、API 端点和测试文件，再与 Android 源码逐项匹配。

来源：
- https://github.com/jason5ng32/MyIP
- https://ipcheck.ing/
