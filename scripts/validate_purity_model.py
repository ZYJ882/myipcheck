from dataclasses import dataclass


@dataclass(frozen=True)
class Scenario:
    name: str
    tor: bool = False
    proxy: bool = False
    vpn: bool = False
    proxy_confirmed_by_two_sources: bool = False
    vpn_confirmed_by_two_sources: bool = False
    abuse_score: int | None = None
    compromised: bool = False
    abuser: bool = False
    scraper: bool = False
    high_external_risk: int | None = None
    hosting_sources: int = 0
    hosting_is_heuristic_only: bool = False
    source_ip_disagreement: bool = False


def calculate(s: Scenario) -> tuple[int, dict[str, int]]:
    anonymity = 0
    if s.tor:
        anonymity = 45
    elif s.proxy:
        anonymity = 38 if s.proxy_confirmed_by_two_sources else 32
    elif s.vpn:
        anonymity = 30 if s.vpn_confirmed_by_two_sources else 25

    abuse_from_score = 0
    if s.abuse_score is not None:
        if s.abuse_score >= 90:
            abuse_from_score = 35
        elif s.abuse_score >= 75:
            abuse_from_score = 28
        elif s.abuse_score >= 50:
            abuse_from_score = 18
        elif s.abuse_score >= 25:
            abuse_from_score = 8

    external = 0
    if s.high_external_risk is not None:
        if s.high_external_risk >= 75:
            external = 15
        elif s.high_external_risk >= 50:
            external = 10
        elif s.high_external_risk >= 25:
            external = 5

    attack = 0
    if s.compromised and s.abuser:
        attack = 24
    elif s.compromised:
        attack = 20
    elif s.abuser:
        attack = 18
    elif s.scraper:
        attack = 6

    abuse = max(abuse_from_score, external, attack)
    independent_abuse_sources = sum(
        [
            abuse_from_score > 0,
            (s.compromised or s.abuser),
            external > 0,
        ]
    )
    if independent_abuse_sources >= 2:
        abuse = min(40, abuse + 4)

    if s.hosting_sources >= 2:
        infrastructure = 12
    elif s.hosting_sources == 1:
        infrastructure = 3 if s.hosting_is_heuristic_only else 7
    else:
        infrastructure = 0

    integrity = 8 if s.source_ip_disagreement else 0
    parts = {
        "匿名化": anonymity,
        "滥用/攻击": abuse,
        "托管基础设施": infrastructure,
        "来源完整性": integrity,
    }
    risk = min(100, sum(parts.values()))
    return 100 - risk, parts


SCENARIOS = [
    Scenario("正常住宅/商业出口"),
    Scenario("单一已验证托管来源", hosting_sources=1),
    Scenario("仅单一 VPN 标记", vpn=True),
    Scenario("单一代理标记", proxy=True),
    Scenario("双来源代理确认", proxy=True, proxy_confirmed_by_two_sources=True),
    Scenario("Tor 官方确认", tor=True),
    Scenario("AbuseIPDB 90 分", abuse_score=90),
    Scenario("受损与滥用双来源确认", compromised=True, abuser=True),
    Scenario("仅供应商高风险分", high_external_risk=80),
    Scenario("Tor + 高滥用 + 双来源托管", tor=True, abuse_score=90, compromised=True, hosting_sources=2),
    Scenario("地理库分歧不参与风险", source_ip_disagreement=False),
]

for scenario in SCENARIOS:
    score, parts = calculate(scenario)
    print(f"{scenario.name}: score={score}, risk={100-score}, {parts}")
