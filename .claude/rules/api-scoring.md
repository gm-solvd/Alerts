---
paths:
  - "api/**/score/**"
  - "api/**/scoring/**"
---

# Scoring Algorithm — API

CVSS-inspired privacy health score.

## Formula

```
Score = max(0, 100 - SUM(penalty(severity) × weight(category) × decay(count)))
```

## Severity Penalties

| Severity | Penalty |
|----------|---------|
| Critical | 15 |
| High | 10 |
| Medium | 5 |
| Low | 2 |

## Category Weights

| Category | Weight |
|----------|--------|
| Breach | 1.0 |
| Network/Vulnerability | 0.9 |
| Identity | 0.9 |
| App/Permissions | 0.7 |
| Tracker | 0.6 |
| Device | 0.5 |
| Social | 0.4 |

## Decay

`1 + ln(count)` — diminishing returns for multiple alerts in same category.

## Score Bands

- 90-100: Excellent (Green)
- 70-89: Good (Blue)
- 50-69: Fair (Yellow)
- 30-49: Poor (Orange)
- 0-29: Critical (Red)

## Recalculation Triggers

Recalculate when: alert created, alert resolved, alert deleted, scan completed.

Full reference: `rules/architecture-api/scoring.md`
