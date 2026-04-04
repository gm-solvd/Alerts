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

- [x] **[STYLE]** Add `@Preview` annotations to all content composables and preview data factories on domain models (from PR #23 review)
- [x] **[STYLE]** Migrate `collectAsState` to `collectAsStateWithLifecycle` across all screens and App.kt (from PR #23 review)
- [x] **[STYLE]** Extract hardcoded `Color(0xFF4CAF50)` (green/success) to `PrivacyAlertColors.Success` — used in ScoreGauge, SeverityBadge, AlertDetailScreen, MitigationsScreen (from PR #26 review)
- [x] **[STYLE]** `OnboardingScreen.kt` — button heights hardcoded as `56.dp` instead of `ComponentSize.buttonHeight`; check for other screens with raw button height dp values (from PR #33 review)
- [x] **[STYLE]** `App.kt` — add `android:configChanges="orientation|screenSize|keyboardHidden"` to `<activity>` in `AndroidManifest.xml` to prevent splash re-showing on screen rotation (from PR #33 review)
- [x] **[STYLE]** `HttpLogger.android.kt` — `Log.d` silently truncates messages beyond ~4000 chars; with `LogLevel.BODY`, large response bodies are cut off in Logcat — split on newlines and call `Log.d` per line (from PR #35 review)
- [x] **[TEST]** `ApiClient.kt` — `statusToAppError` range-based mapping (`400..499`, `500..599`) and specific status overrides have no unit tests — add parameterised tests verifying each boundary maps to the correct `AppError` subtype (from PR #35 review)
- [x] **[TEST]** `ErrorMessages.kt` — `toUserMessage()` has no unit tests — add parameterised test verifying each `AppError` subtype maps to the expected string and unknown Throwable falls back correctly (from PR #36 review)
- [x] **[STYLE]** `ErrorMessages.kt` — user-facing strings are hardcoded English; add `expect fun errorString(key: ErrorStringKey): String` with `actual` using `strings.xml` (Android) and `Localizable.strings` (iOS) for proper i18n (from PR #36 review)
- [x] **[ARCH]** `DashboardViewModel`, `AlertsViewModel` — `AppError.Unauthorized` sets `Error(message)` state but does not trigger navigation to login; add a `SessionExpired` UiState variant so screens can auto-navigate on token expiry (from PR #36 review)
- [x] **[ARCH]** `DashboardViewModel.kt:5`, `AuthViewModel.kt:5` — ViewModels import `TokenStorage` from `data.local` directly (presentation→data layer violation). Create domain use cases (`GetUserEmailUseCase`, `SaveUserEmailUseCase`) or move `TokenStorage` interface to `domain.repository` (from PR #41 review)
- [x] **[PERF]** `DashboardViewModel.kt:startFixAll()` — Sequential mitigation completion in a loop; use `async/awaitAll` for parallel execution when many mitigations exist (from PR #41 review)
- [x] **[STYLE]** `ShieldLogo.kt:18` — Unused import of `AppColors` (file only uses `MaterialTheme.colorScheme`) (from PR #41 review)
