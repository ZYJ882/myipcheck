"""MyIPCheck v3.2 公开网络出口风险模型的独立场景验证。

该脚本镜像 Android 端的关键数学不变量：主分只来自公开滥用证据；网络
透明度和基础设施属性独立显示；每一个分值由来源质量、计数饱和及时间衰减
连续计算；字段缺失降低覆盖度而不静默降低风险。
"""

from __future__ import annotations

from dataclasses import dataclass
from math import exp


@dataclass
class Scenario:
    name: str
    compromised: bool = False
    attack_events: int = 0
    abuse_score: int = 0
    abuse_reports: int = 0
    abuse_distinct_users: int = 0
    abuse_last_seen_days: int | None = None
    abuser: bool = False
    crawler: bool = False
    vendor_risk: int = 0
    proxy_last_seen_days: int | None = None
    tor_official: bool = False
    proxy_sources: int = 0
    vpn_sources: int = 0
    relay: bool = False
    behavior_coverage: float = 1.0
    anonymity_coverage: float = 1.0
    context_coverage: float = 1.0
    observability_covered: bool = True


def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def volume(count: int, tau: float) -> float:
    return clamp(1.0 - exp(-max(count, 0) / tau), 0.0, 1.0)


def decay(days: int | None, half_life: float, unknown: float = 0.60) -> float:
    return clamp(2.0 ** (-max(days, 0) / half_life), 0.0, 1.0) if days is not None else unknown


def evidence(cap: float, quality: float, count: int, tau: float, days: int | None, half_life: float) -> float:
    return clamp(cap * quality * volume(count, tau) * decay(days, half_life), 0.0, 0.95)


def direct_risk(s: Scenario) -> float:
    compromised = evidence(0.95, 0.60, 1, 1.0, s.proxy_last_seen_days, 120.0) if s.compromised else 0.0
    attacks = evidence(0.80, 0.45, s.attack_events, 3.0, s.proxy_last_seen_days, 45.0) if s.attack_events else 0.0
    # v3.2: independent reporters carry the signal; same-source report volume is weak support.
    support = max(s.abuse_distinct_users, 0) + int(max(s.abuse_reports - s.abuse_distinct_users, 0) * 0.25)
    abuse = (
        0.60
        * 0.75
        * (clamp(s.abuse_score, 0, 100) / 100.0) ** 1.3
        * volume(support, 5.0)
        * decay(s.abuse_last_seen_days, 60.0)
        if s.abuse_score > 0 and support > 0
        else 0.0
    )
    abuser = evidence(0.60, 0.65, 1, 1.0, None, 60.0) if s.abuser else 0.0
    crawler = evidence(0.20, 0.60, 1, 1.0, None, 14.0) if s.crawler else 0.0
    classified_proxy = s.compromised or s.attack_events > 0 or s.crawler
    generic = 0.25 * 0.35 * (clamp(s.vendor_risk, 0, 100) / 100.0) ** 1.2 * decay(s.proxy_last_seen_days, 30.0) if s.vendor_risk and not classified_proxy else 0.0
    families = [compromised, attacks, max(abuse, abuser), crawler, generic]
    return clamp(85.0 * (1.0 - __import__('functools').reduce(lambda product, value: product * (1.0 - clamp(value, 0.0, 0.95)), families, 1.0)), 0.0, 85.0)


def transparency(s: Scenario) -> float:
    if s.tor_official:
        return 100.0
    if s.proxy_sources >= 2:
        return 75.0
    if s.proxy_sources == 1:
        return 60.0
    if s.vpn_sources >= 2:
        return 55.0
    if s.vpn_sources == 1:
        return 45.0
    if s.relay:
        return 35.0
    return 0.0


def calculate(s: Scenario) -> tuple[float, float, float]:
    risk = direct_risk(s)
    coverage = 55 * s.behavior_coverage + 25 * s.anonymity_coverage + 15 * s.context_coverage + (5 if s.observability_covered else 0)
    return 100.0 - risk, transparency(s), coverage


all_clear = Scenario('全覆盖且未见风险')
missing = Scenario('关键字段缺失', behavior_coverage=0.0, anonymity_coverage=0.0, context_coverage=0.0, observability_covered=False)
abuse_30 = Scenario('AbuseIPDB 30 分', abuse_score=30, abuse_reports=2, abuse_distinct_users=2, abuse_last_seen_days=10)
abuse_31 = Scenario('AbuseIPDB 31 分', abuse_score=31, abuse_reports=2, abuse_distinct_users=2, abuse_last_seen_days=10)
old_abuse = Scenario('相同 AbuseIPDB 分但更旧', abuse_score=31, abuse_reports=2, abuse_distinct_users=2, abuse_last_seen_days=120)
report_volume_only = Scenario('同一来源重复报告', abuse_score=90, abuse_reports=100, abuse_distinct_users=1, abuse_last_seen_days=1)
many_independent = Scenario('同等数量独立报告者', abuse_score=90, abuse_reports=100, abuse_distinct_users=100, abuse_last_seen_days=1)
proxy_only = Scenario('代理属性', proxy_sources=1)
tor_only = Scenario('Tor 属性', tor_official=True)
severe = Scenario('多家族高强度行为证据', compromised=True, attack_events=8, abuse_score=90, abuse_reports=30, abuse_distinct_users=10, abuse_last_seen_days=1, abuser=True, crawler=True, vendor_risk=80, proxy_last_seen_days=1)

for scenario in [all_clear, missing, abuse_30, abuse_31, old_abuse, report_volume_only, many_independent, proxy_only, tor_only, severe]:
    score, transparent, covered = calculate(scenario)
    print(f'{scenario.name}: 主分={score:.1f}, 透明度={transparent:.1f}, 覆盖度={covered:.1f}')

assert calculate(all_clear)[0] == calculate(missing)[0] == 100.0, '字段缺失不能被当作风险或安全信号'
assert calculate(missing)[2] < calculate(all_clear)[2], '字段缺失必须降低覆盖度'
assert calculate(abuse_31)[0] < calculate(abuse_30)[0], '相邻原始分必须产生连续、可观察的主分变化'
assert calculate(old_abuse)[0] > calculate(abuse_31)[0], '更久远的同类报告必须衰减'
assert calculate(report_volume_only)[0] > calculate(many_independent)[0], '同一来源重复报告的风险必须低于同等数量独立报告者'
assert calculate(proxy_only)[0] == 100.0 and calculate(proxy_only)[1] == 60.0, '代理属性只能进入透明度，不得污染主分'
assert calculate(tor_only)[0] == 100.0 and calculate(tor_only)[1] == 100.0, 'Tor 官方确认只能进入透明度，不得污染主分'
assert calculate(severe)[0] >= 15.0, '主风险须保留不确定性上限，不应宣称绝对 0 分'
assert 0.0 <= calculate(severe)[0] <= 100.0 and 0.0 <= calculate(severe)[2] <= 100.0, '输出必须限制在合法范围'
print('v3.2 连续性、独立性、去重、隔离性、覆盖度与上限断言：通过')
