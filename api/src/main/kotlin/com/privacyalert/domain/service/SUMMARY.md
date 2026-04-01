# Domain Services

Pure Kotlin business logic. No Spring annotations — classes are instantiated directly in tests. Each service depends only on domain interfaces (repository interfaces, scanner interfaces, JwtProvider, PasswordEncoder).

## Files

| File | Description |
|------|-------------|
| `AlertService.kt` | Alert CRUD: list alerts with optional category/severity filters + pagination, find by ID, `resolveAlert()` (sets resolved=true, triggers score recalculation), delete alert |
| `AdminService.kt` | Admin-only operations: create user, list all users with alert stats, get user detail with recent alerts, delete user, trigger manual scan for a user, get scan history, get platform-wide stats (totals + breakdown by category/severity) |
| `AuthService.kt` | Registration (hash password, persist user), login (verify credentials, issue tokens), OAuth callback (upsert user via provider+sub, issue tokens), refresh (validate refresh token hash, revoke old, issue new pair — rotation strategy) |
| `MitigationService.kt` | Find mitigations by userId or alertId, mark a mitigation as completed |
| `PermissionAuditService.kt` | Receives Android permissions list; flags risky permissions (CAMERA, RECORD_AUDIO, ACCESS_FINE_LOCATION, READ_CONTACTS, READ_CALL_LOG, READ_SMS, BODY_SENSORS) as MEDIUM `APP_OVERPERMISSIONS` alerts |
| `ScanService.kt` | Orchestrates four scanner types in sequence: `breachScan`, `identityScan`, `piiExposureScan`, `socialFootprintScan`, `fullScan` (all four). Each scan creates domain alerts and persists a `ScanResult` |
| `ScoreService.kt` | Computes privacy health score using CVSS-inspired deduction: `score = max(0, 100 - Σ(penalty × categoryWeight × (1 + ln(count))))`. Persists `ScoreRecord` on each calculation. Called by `resolveAlert` and after each scan |

## Scanner Interfaces

| File | Description |
|------|-------------|
| `BreachScanner.kt` | Interface: `scanEmail(email)`, `scanPhone(phone)` → list of `KnownBreach` |
| `IdentityExposureScanner.kt` | Interface: `scan(profile)` → list of detected identity exposures |
| `PiiExposureScanner.kt` | Interface: `scan(profile)` → list of PII exposure findings |
| `SocialFootprintScanner.kt` | Interface: `scan(profile)` → list of discovered social profiles |

## Provider Interfaces

| File | Description |
|------|-------------|
| `JwtProvider.kt` | Interface: `generateAccessToken(userId)`, `generateRefreshToken(userId)`, `validateToken(token)`, `getUserId(token)` |
| `OAuthVerifier.kt` | Interface: `verify(provider, idToken)` → (sub, email, name) |
| `PasswordEncoder.kt` | Interface: `encode(raw)`, `matches(raw, encoded)` |

## Scoring Algorithm

```
penalty(alert) = severity.points × category.weight
decay(n)       = 1 + ln(n)   // diminishing returns per category
score          = max(0, 100 - Σ_category [ penalty × decay(alertCount) ])
```
