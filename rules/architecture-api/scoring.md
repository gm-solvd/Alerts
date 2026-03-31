# Exposure Score Algorithm

## Overview
CVSS-inspired deduction model. Score starts at 100 (excellent) and decreases based on unresolved alerts.

---

## Formula

```
Score = max(0, 100 - SUM( penalty(severity) * weight(category) * decay(count) ))
```

Where for each threat category:
- `penalty(severity)` — base point deduction per severity tier
- `weight(category)` — importance multiplier for the category
- `decay(count)` — diminishing returns for multiple alerts in the same category
- `count` — number of unresolved alerts in that category

---

## Severity Penalties

Based on CVSS qualitative ranges, normalized to 0-100 scale:

| Severity | Penalty per alert |
|----------|-------------------|
| Critical | 15 |
| High     | 10 |
| Medium   | 5  |
| Low      | 2  |

---

## Category Weights

Reflect real-world impact on user privacy (CIA triad prioritization):

| Category              | Severity    | Weight |
|-----------------------|-------------|--------|
| Data breach           | Critical    | 1.0    |
| Network vulnerability | High        | 0.9    |
| Identity exposure     | High        | 0.9    |
| App overpermissions   | Medium      | 0.7    |
| Tracker exposure      | Medium      | 0.6    |
| Device hygiene        | Medium      | 0.5    |
| Social footprint      | Low-Medium  | 0.4    |

---

## Diminishing Returns

Multiple alerts in the same category use logarithmic decay to prevent score destruction:

```
decay(n) = 1 + ln(n)    where n >= 1
```

| Count | Factor |
|-------|--------|
| 1     | 1.00   |
| 2     | 1.69   |
| 3     | 2.10   |
| 5     | 2.61   |
| 10    | 3.30   |

---

## Worked Example

User has: 2 data breaches, 1 network vulnerability, 3 tracker exposures, 1 device hygiene issue.

```
Data breach:    15 * 1.0 * (1 + ln(2))  = 15 * 1.69  = 25.4
Network vuln:   10 * 0.9 * (1 + ln(1))  = 10 * 0.9   =  9.0
Tracker:         5 * 0.6 * (1 + ln(3))  =  3 * 2.10  =  6.3
Device hygiene:  5 * 0.5 * (1 + ln(1))  =  5 * 0.5   =  2.5

Total deductions = 43.2
Score = max(0, 100 - 43.2) = 57
```

---

## Score Bands

| Range  | Label     | Color       |
|--------|-----------|-------------|
| 90-100 | Excellent | Green       |
| 70-89  | Good      | Light green |
| 50-69  | Fair      | Yellow      |
| 30-49  | Poor      | Orange      |
| 0-29   | Critical  | Red         |

---

## User-Facing Explanation

> "Your Privacy Health Score starts at 100. Each unresolved alert lowers your score based on how serious it is and what type of threat it represents. Fixing alerts raises your score back up."

---

## Recalculation Triggers
- Alert created
- Alert resolved
- Alert deleted
- Scan completed (breach, identity, permission audit)
