"""MyIPCheck v3 透明评分模型的独立场景验证。

该脚本镜像 Android 端的主要数学规则，用于验证连续性、类别上限及
“缺失不等于无风险，覆盖度必须单列”的产品约束。它不是第三方风控算法。
"""

from __future__ import annotations

from dataclasses import dataclass
from math import exp, log


@dataclass
class Scenario:
    name: str
    abuse_score: int | None = None
    abuse_reports: int | None = None
    abuse_last_report_days: int | None = None
    proxy_risk: int | None = None
    proxy_confidence: int | None = None
    proxy_last_seen_days: int | None = None
    proxy_external: bool = False
    proxy_ipapi: bool = False
    vpn_external: bool = False
    vpn_ipapi: bool = False
    tor_official: bool = False
    tor_other: bool = False
    compromised: bool = False
    abuser: bool = False
    external_crawler: bool = False
    ipapi_crawler: bool = False
    attack_events: int = 0
    proxy_hosting: bool = False
    ipapi_datacenter: bool = False
    heuristic_hosting: bool = False
    source_ip_disagreement: bool = False
    behavior_covered: bool = True
    anonymity_covered: bool = True
    context_covered: bool = True
    observability_covered: bool = True


def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def freshness(days: int | None, fallback: float) -> float:
    if days is None:
        return fallback
    return clamp(0.45 + 0.55 * exp(-max(days, 0) / 21.0), 0.45, 1.0)


def proxy_reliability(s: Scenario) -> float:
    confidence = 0.60 if s.proxy_confidence is None else clamp(s.proxy_confidence, 0, 100) / 100.0
    return clamp((0.30 + 0.70 * confidence) * freshness(s.proxy_last_seen_days, 0.70), 0.25, 1.0)


def calculate(s: Scenario) -> tuple[float, dict[str, float]]:
    reliability = proxy_reliability(s)

    proxy_risk = (
        max(9.0, 10.0 * reliability) + 2.0 * reliability
        if s.proxy_external and s.proxy_ipapi
        else 10.0 * reliability
        if s.proxy_external
        else 9.0
        if s.proxy_ipapi
        else 0.0
    )
    vpn_risk = (
        max(7.0, 8.0 * reliability) + 1.5 * reliability
        if s.vpn_external and s.vpn_ipapi
        else 8.0 * reliability
        if s.vpn_external
        else 7.0
        if s.vpn_ipapi
        else 0.0
    )
    transparency = (
        15.0
        if s.tor_official
        else 13.0 + 1.5 * reliability
        if s.tor_other
        else max(proxy_risk, vpn_risk)
    )
    transparency = clamp(transparency, 0.0, 15.0)

    abuse_base = 0.0 if s.abuse_score is None else 34.0 * (clamp(s.abuse_score, 0, 100) / 100.0) ** 1.35
    abuse_volume = 0.0 if s.abuse_reports is None else 2.0 * log(1 + max(s.abuse_reports, 0)) / log(101.0)
    abuse_freshness = 0.0 if not s.abuse_score else 2.0 * freshness(s.abuse_last_report_days, 0.60)
    abuse_from_score = min(36.0, abuse_base + abuse_volume + abuse_freshness)
    vendor_risk = 0.0 if s.proxy_risk is None else 16.0 * (clamp(s.proxy_risk, 0, 100) / 100.0) ** 1.20 * reliability
    compromised_risk = 12.0 + 8.0 * reliability if s.compromised else 0.0
    abuser_risk = 16.0 if s.abuser else 0.0
    crawler_risk = 2.0 + 4.0 * reliability if s.external_crawler else 4.0 if s.ipapi_crawler else 0.0
    history_risk = 0.0 if s.attack_events <= 0 else 4.0 * log(1 + s.attack_events) / log(101.0) * reliability
    direct_attack = min(
        28.0,
        max(compromised_risk, abuser_risk, crawler_risk)
        + (3.0 if s.compromised and s.abuser else 0.0)
        + history_risk,
    )
    primary = clamp(max(abuse_from_score / 36.0, vendor_risk / 16.0, direct_attack / 28.0), 0.0, 1.0)
    independent = [
        abuse_from_score / 36.0 if abuse_from_score else None,
        max(vendor_risk / 16.0, compromised_risk / 20.0) if max(vendor_risk, compromised_risk) else None,
        abuser_risk / 16.0 if abuser_risk else None,
    ]
    independent = [value for value in independent if value is not None]
    corroboration = min(0.20, 0.08 + 0.12 * sum(independent) / len(independent)) if len(independent) >= 2 else 0.0
    crawler_support = min(0.08, crawler_risk / 50.0) if crawler_risk and primary > crawler_risk / 28.0 else 0.0
    behavior = clamp(70.0 * (1.0 - (1.0 - primary) * (1.0 - corroboration) * (1.0 - crawler_support)), 0.0, 70.0)

    proxy_hosting_risk = 2.0 + 2.0 * reliability if s.proxy_hosting else 0.0
    ipapi_hosting_risk = 4.5 if s.ipapi_datacenter else 0.0
    hosting_corroboration = 1.5 + 2.0 * reliability if proxy_hosting_risk and ipapi_hosting_risk else 0.0
    context = min(10.0, max(2.0 if s.heuristic_hosting else 0.0, proxy_hosting_risk, ipapi_hosting_risk) + hosting_corroboration)
    observability = 5.0 if s.source_ip_disagreement else 0.0

    coverage = (
        70.0 * s.behavior_covered
        + 15.0 * s.anonymity_covered
        + 10.0 * s.context_covered
        + 5.0 * s.observability_covered
    )
    parts = {
        "直接恶意与滥用": behavior,
        "匿名化/透明度": transparency,
        "网络上下文": context,
        "出口可观测性": observability,
        "证据覆盖度": coverage,
    }
    risk = clamp(behavior + transparency + context + observability, 0.0, 100.0)
    return 100.0 - risk, parts


SCENARIOS = [
    Scenario("全覆盖且未见风险"),
    Scenario("字段缺失但未见风险", behavior_covered=False, anonymity_covered=False, context_covered=False),
    Scenario("AbuseIPDB 30 分、1 报告、60 天前", abuse_score=30, abuse_reports=1, abuse_last_report_days=60),
    Scenario("AbuseIPDB 31 分、1 报告、60 天前", abuse_score=31, abuse_reports=1, abuse_last_report_days=60),
    Scenario("单源代理、40% 置信度、30 天前", proxy_external=True, proxy_confidence=40, proxy_last_seen_days=30),
    Scenario("单源代理、90% 置信度、1 天前", proxy_external=True, proxy_confidence=90, proxy_last_seen_days=1),
    Scenario("Tor 官方确认", tor_official=True),
    Scenario("受损、攻击历史 8 次", compromised=True, attack_events=8, proxy_confidence=80, proxy_last_seen_days=2),
    Scenario("多重高风险与双源托管", abuse_score=90, abuse_reports=80, abuse_last_report_days=1, proxy_risk=80, proxy_confidence=90, proxy_last_seen_days=1, compromised=True, abuser=True, proxy_hosting=True, ipapi_datacenter=True),
]

for scenario in SCENARIOS:
    score, parts = calculate(scenario)
    detail = ", ".join(f"{name}={value:.1f}" for name, value in parts.items())
    print(f"{scenario.name}: score={score:.1f}, risk={100-score:.1f}, {detail}")

score_30, _ = calculate(SCENARIOS[2])
score_31, _ = calculate(SCENARIOS[3])
low_conf_proxy, _ = calculate(SCENARIOS[4])
high_conf_proxy, _ = calculate(SCENARIOS[5])
missing_score, missing_parts = calculate(SCENARIOS[1])
full_score, full_parts = calculate(SCENARIOS[0])
_, high_parts = calculate(SCENARIOS[-1])

assert score_31 < score_30, "AbuseIPDB 相邻原始分必须改变最终分数"
assert high_conf_proxy < low_conf_proxy, "更高置信度且更新鲜的同一代理结论必须产生更高风险"
assert missing_score == full_score == 100.0, "缺失字段不应自动扣风险"
assert missing_parts["证据覆盖度"] < full_parts["证据覆盖度"], "字段缺失必须降低覆盖度而非静默显示安全"
assert high_parts["直接恶意与滥用"] <= 70.0, "行为风险桶必须受 70 分上限限制"
assert high_parts["网络上下文"] <= 10.0, "网络上下文桶必须受 10 分上限限制"
assert high_parts["匿名化/透明度"] <= 15.0, "透明度桶必须受 15 分上限限制"
assert high_parts["出口可观测性"] <= 5.0, "出口观测桶必须受 5 分上限限制"
print("连续性、分层上限与覆盖度断言：通过")
