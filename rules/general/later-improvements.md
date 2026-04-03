# Later Improvements

Tracked improvements identified during automated code reviews. Each entry comes from a NIT finding that indicates a pattern worth addressing across the codebase.

## Criteria

An item belongs here if it:
- Reveals a pattern that should be fixed across the codebase (not just one spot)
- Identifies tech debt that could cause issues as the codebase grows
- Suggests a refactor that improves maintainability but isn't urgent
- Identifies missing test coverage for non-critical paths

An item does NOT belong here if it:
- Is purely cosmetic and already passing detekt/lint
- Is a subjective style preference with no clear benefit
- Was already fixed as part of a BLOCKER/WARNING fix

## When to execute

After all main tasks in a feature are complete (all PRs merged), work through this list top-to-bottom. Each fix follows the normal workflow: branch, implement, validate, commit, PR, review.

---

## Items

- [ ] **[STYLE]** Add `@Preview` annotations to all content composables and preview data factories on domain models (from PR #23 review)
- [ ] **[STYLE]** Migrate `collectAsState` to `collectAsStateWithLifecycle` across all screens and App.kt (from PR #23 review)
- [ ] **[STYLE]** Extract hardcoded `Color(0xFF4CAF50)` (green/success) to `PrivacyAlertColors.success` — used in ScoreGauge, SeverityBadge, AlertDetailScreen (from PR #26 review)
