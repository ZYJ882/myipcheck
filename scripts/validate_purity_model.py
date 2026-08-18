from __future__ import annotations

from dataclasses import dataclass
from math import exp, log


@dataclass(frozen=True)
class Scenario:
    name: str
    tor_official: bool = False
    tor_other: bool = False
    proxy_external: bool = False
    proxy_ipapi: bool = False
    vpn_external: bool = False
    vpn_ipapi: bool = False
    proxy_confidence: int | None = None
    proxy_last_seen_days: int | None = None
    abuse_score: int | None = None
    abuse_reports: int = 0
    abuse_last_report_days: int | None = None
    proxy_risk: int | None = None
    compromised: bool = False
    abuser: bool = False
    external_crawler: bool = False
    ipapi_crawler: bool = False
    attack_events: int = 0
    proxy_hosting: bool = False
    ipapi_datacenter: bool = False
    heuristic_hosting: bool = False
    source_ip_disagreement: bool = False


def clamp(value: float, lower: float, upper: float) -> float:
    return max(lower, min(upper, value))


def freshness(days: int | None, fallback: float = 0.70) -> float:
    if days is None:
        return fallback
    return clamp(0.45 + 0.55 * exp(-max(days, 0) / 21.0), 0.45, 1.0)


def proxy_reliability(s: Scenario) -> float:
    confidence = (s.proxy_confidence if s.proxy_confidence is not None else 60) / 100.0
    return clamp((0.30 + 0.70 * clamp(confidence, 0.0, 1.0)) * freshness(s.proxy_last_seen_days), 0.25, 1.0)


def calculate(s: Scenario) -> tuple[float, dict[str, float]]:
    reliability = proxy_reliability(s)

    proxy_risk = (
        max(28.0, 32.0 * reliability) + 6.0 * reliability
        if s.proxy_external and s.proxy_ipapi
        else 32.0 * reliability
        if s.proxy_external
        else 28.0
        if s.proxy_ipapi
        else 0.0
    )
    vpn_risk = (
        max(22.0, 25.0 * reliability) + 5.0 * reliability
        if s.vpn_external and s.vpn_ipapi
        else 25.0 * reliability
        if s.vpn_external
        else 22.0
        if s.vpn_ipapi
        else 0.0
    )
    anonymity = (
        45.0
        if s.tor_official
        else 38.0 + 4.0 * reliability
        if s.tor_other
        else max(proxy_risk, vpn_risk)
    )
    anonymity = clamp(anonymity, 0.0, 45.0)

    abuse_base = 0.0 if s.abuse_score is None else 34.0 * (clamp(s.abuse_score, 0, 100) / 100.0) ** 1.35
    abuse_volume = 2.0 * log(1 + max(s.abuse_reports, 0)) / log(101.0)
    abuse_freshness = 0.0 if not s.abuse_score else 2.0 * freshness(s.abuse_last_report_days, 0.60)
    abuse_from_score = min(36.0, abuse_base + abuse_volume + abuse_freshness)

    vendor_risk = 0.0 if s.proxy_risk is None else 16.0 * (clamp(s.proxy_risk, 0, 100) / 100.0) ** 1.20 * reliability
    compromised_risk = 12.0 + 8.0 * reliability if s.compromised else 0.0
    abuser_risk = 16.0 if s.abuser else 0.0
    crawler_risk = 2.0 + 4.0 * reliability if s.external_crawler else 4.0 if s.ipapi_crawler else 0.0
    history_risk = 0.0 if s.attack_events <= 0 else 4.0 * log(1 + s.attack_events) / log(101.0) * reliability
    direct_attack = min(28.0, max(compromised_risk, abuser_risk, crawler_risk) + (3.0 if s.compromised and s.abuser else 0.0) + history_risk)
    primary_abuse = max(abuse_from_score, vendor_risk, direct_attack)
    independent = [
        abuse_from_score / 36.0 if abuse_from_score else None,
        max(vendor_risk / 16.0, compromised_risk / 20.0) if max(vendor_risk, compromised_risk) else None,
        abuser_risk / 16.0 if abuser_risk else None,
    ]
    independent = [x for x in independent if x is not None]
    corroboration = min(4.0, 1.0 + 3.0 * sum(independent) / len(independent)) if len(independent) >= 2 else 0.0
    crawler_increment = min(4.0, crawler_risk) if crawler_risk and primary_abuse > crawler_risk else 0.0
    abuse = min(40.0, primary_abuse + corroboration + crawler_increment)

    proxy_hosting_risk = 4.0 + 3.0 * reliability if s.proxy_hosting else 0.0
    ipapi_hosting_risk = 6.5 if s.ipapi_datacenter else 0.0
    hosting_corroboration = 2.5 + 3.0 * reliability if proxy_hosting_risk and ipapi_hosting_risk else 0.0
    infrastructure = min(12.0, max(3.0 if s.heuristic_hosting else 0.0, proxy_hosting_risk, ipapi_hosting_risk) + hosting_corroboration)

    integrity = 8.0 if s.source_ip_disagreement else 0.0
    parts = {"匿名化": anonymity, "滥用/攻击": abuse, "托管基础设施": infrastructure, "来源完整性": integrity}
    risk = min(100.0, sum(parts.values()))
    return 100.0 - risk, parts


SCENARIOS = [
    Scenario("正常住宅/商业出口"),
    Scenario("AbuseIPDB 30 分、1 报告、60 天前", abuse_score=30, abuse_reports=1, abuse_last_report_days=60),
    Scenario("AbuseIPDB 31 分、1 报告、60 天前", abuse_score=31, abuse_reports=1, abuse_last_report_days=60),
    Scenario("AbuseIPDB 60 分、20 报告、14 天前", abuse_score=60, abuse_reports=20, abuse_last_report_days=14),
    Scenario("ProxyCheck 40 分、40% 置信度、30 天前", proxy_risk=40, proxy_confidence=40, proxy_last_seen_days=30),
    Scenario("ProxyCheck 40 分、90% 置信度、1 天前", proxy_risk=40, proxy_confidence=90, proxy_last_seen_days=1),
    Scenario("单源代理、40% 置信度、30 天前", proxy_external=True, proxy_confidence=40, proxy_last_seen_days=30),
    Scenario("单源代理、90% 置信度、1 天前", proxy_external=True, proxy_confidence=90, proxy_last_seen_days=1),
    Scenario("双源代理、90% 置信度、1 天前", proxy_external=True, proxy_ipapi=True, proxy_confidence=90, proxy_last_seen_days=1),
    Scenario("Tor 官方确认", tor_official=True),
    Scenario("受损、攻击历史 8 次", compromised=True, attack_events=8, proxy_confidence=80, proxy_last_seen_days=2),
    Scenario("AbuseIPDB + ProxyCheck + 双源托管", abuse_score=90, abuse_reports=80, abuse_last_report_days=1, proxy_risk=80, proxy_confidence=90, proxy_last_seen_days=1, compromised=True, proxy_hosting=True, ipapi_datacenter=True),
]

for scenario in SCENARIOS:
    score, parts = calculate(scenario)
    detail = ", ".join(f"{name}={value:.1f}" for name, value in parts.items())
    print(f"{scenario.name}: score={score:.1f}, risk={100-score:.1f}, {detail}")

score_30, _ = calculate(SCENARIOS[1])
score_31, _ = calculate(SCENARIOS[2])
low_conf_proxy, _ = calculate(SCENARIOS[6])
high_conf_proxy, _ = calculate(SCENARIOS[7])
assert score_31 < score_30, "AbuseIPDB 相邻原始分必须改变最终分数"
assert high_conf_proxy < low_conf_proxy, "更高置信度且更新鲜的同一代理结论必须产生更高风险"
assert calculate(SCENARIOS[10])[1]["滥用/攻击"] <= 40.0, "滥用桶必须受 40 分上限限制"
assert calculate(SCENARIOS[-1])[1]["托管基础设施"] <= 12.0, "基础设施桶必须受 12 分上限限制"
print("连续性与上限断言：通过")
