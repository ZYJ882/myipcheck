# 用户指定站点的集成性核验

核验时间：2026-08-27（GMT+8）

## myip.edgeone.ai

该主机名在当前网络环境中 DNS 无法解析（`ERR_NAME_NOT_RESOLVED`）。因此不能把它作为本版本的默认请求地址或评分数据源。后续如提供可解析的官方接口域名、API 文档与服务条款，可作为可选来源重新评估。

## BrowserLeaks

站点首页说明其是通过网页 JavaScript、WebRTC、Canvas、WebGL、字体、TLS ClientHello、HTTP/2、DNS 等测试浏览器隐私与指纹泄漏的工具集。它适合在浏览器内执行交互式环境自检，并未在首页公开 Android 原生可调用的风险评分 API。

结论：Android 原生 APP 不应冒充已经执行了 BrowserLeaks 的 JavaScript / WebRTC / Canvas / WebGL 测试。可提供“在浏览器中打开 BrowserLeaks 进行网页专属自检”的外部入口，并把该类结果与 IP 历史滥用评分分开。

## NSTool

页面只展示指向 APK 下载、帮助视频和捐赠的链接，没有披露可调用的 IP/网络情报 API、字段、鉴权方式或隐私声明。

结论：不能将 NSTool 作为默认 API 或评分来源，也不应下载或执行该网站提供的 APK。可以在日后获得其公开、可审计 API 文档后重新评估。

## 参考

- https://browserleaks.com/
- https://nstool.netlify.app/
- https://myip.edgeone.ai/（本次 DNS 未解析）
