# MVP Progress

## API

| # | Phase | Status | Date | Notes |
|---|-------|--------|------|-------|
| 1 | Project scaffold + Flyway | DONE | 2026-03-31 | |
| 2 | Domain models + enums + exceptions | DONE | 2026-03-31 | |
| 3 | Data layer (entities, repos, mappers) | DONE | 2026-03-31 | |
| 4 | Auth (register/login/JWT) | DONE | 2026-03-31 | |
| 5 | Alerts CRUD | DONE | 2026-03-31 | |
| 6 | Score calculation | DONE | 2026-03-31 | |
| 7 | Breach scan (HIBP) | DONE | 2026-03-31 | |
| 8 | Mitigations | DONE | 2026-03-31 | |
| 9 | Permission audit | DONE | 2026-03-31 | |
| 10 | Identity scan + OAuth | DONE | 2026-03-31 | OAuth verification is a stub (TODO) |
| 11 | Test coverage | DONE | 2026-04-02 | 86 integration tests (full HTTP→DB) + unit tests. See api/TEST_REPORT.md |

## Mobile

| # | Phase | Status | Date | Notes |
|---|-------|--------|------|-------|
| 1 | Project scaffold (KMP + CMP) | DONE | 2026-04-02 | KMP shared + Android app, Ktor, Koin, Voyager, SQLDelight, theme, MVVM |
| 2 | Auth flow (login/register) | DONE | 2026-04-03 | LoginScreen, RegisterScreen, DashboardScreen placeholder, auth-gate Navigator |
| 3 | Dashboard + score | DONE | 2026-04-03 | Score gauge, severity badges, pull-to-refresh, logout, alert summary cards |
| 4 | Alerts feed + detail | DONE | 2026-04-03 | AlertsScreen with severity filters + pagination, AlertDetailScreen with mitigations, Dashboard navigation |
| 5 | Scan (breach + identity + permissions) | DONE | 2026-04-03 | ScanScreen with email input, scanning progress, results display, Dashboard "Run Scan" button |
| 6 | Fix It (mitigations) | DONE | 2026-04-03 | MitigationsScreen with To Do/Completed sections, mark-as-done, Dashboard "Fix It" button |
| 7 | Styling + dark mode | DONE | 2026-04-03 | Bottom nav with 4 tabs, tab-based routing, dark mode via existing theme |
| 8 | Swiss-clean redesign | DONE | 2026-04-04 | Theme overhaul (blue #2563EB primary, white bg), 4→2 tab nav (Dashboard+Alerts), Dashboard 3-state UI (scan/fix progress), dead code cleanup (ScanScreen, MitigationsScreen, ScanViewModel, MitigationsViewModel), TokenStorage email for auto-scan. PR #41 |
| 9 | Screenshot tests (Roborazzi) | DONE | 2026-04-04 | 23 screenshot tests across 6 screens (Dashboard×8, Alerts×5, AlertDetail×4, Login×3, Register×2, Onboarding×1). JVM-based, no emulator. TestApplication with no Koin. PR #43 |
| 10 | i18n + SessionExpired | DONE | 2026-04-04 | ErrorMessages i18n via expect/actual ErrorStringKey + errorString(). SessionExpired UiState variant in Dashboard/Alerts/AlertDetail with auto-logout on 401. 3 new screenshot tests. |
| 11 | iOS via Compose Multiplatform | DONE | 2026-04-05 | Moved 20 UI files from androidApp to shared/commonMain. expect/actual for OnboardingPreferences + PlatformThemeEffect. iOS entry: MainViewController + AppDelegate.swift + xcodegen project.yml. PR #61 |

Status: `-` (not started) | `IN PROGRESS` | `DONE` | `BLOCKED`
