# API Test Report

Full-stack integration tests using Testcontainers PostgreSQL + `@SpringBootTest(RANDOM_PORT)`.
Run with: `./gradlew test --tests "com.privacyalert.integration.*"`

---

## Auth — `AuthIntegrationTest` (11 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| register returns 201 with access and refresh tokens | new email | 201 + tokens |
| register persists user and allows login | register then login | 200 + tokens |
| register returns 409 for duplicate email | same email twice | 409 |
| register returns 400 for invalid email format | `not-an-email` | 400 |
| register returns 400 for password shorter than 8 chars | 5-char password | 400 |
| login returns 200 with tokens for valid credentials | correct email + password | 200 + tokens |
| login returns 401 for wrong password | wrong password | 401 |
| login returns 401 for nonexistent email | unknown email | 401 |
| refresh returns 200 with new token pair | valid refresh token | 200 + new tokens |
| refresh returns 401 for invalid refresh token | garbage token | 401 |
| full auth flow register then access protected endpoint | register → GET /score | 200 |

---

## Alerts — `AlertIntegrationTest` (10 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| list alerts returns paginated results for authenticated user | user1 has 6 alerts | 200 + page of 6 |
| list alerts filters by category | `?category=DATA_BREACH` | 2 alerts |
| list alerts filters by severity | `?severity=CRITICAL` | 1 alert |
| list alerts returns empty for user with no alerts | user2 has none | 200 + empty content |
| get alert by id returns correct fields | alert aaaa1111-…-001 | 200 + DATA_BREACH/CRITICAL |
| get alert returns 404 for another users alert | user2 fetches user1's alert | 404 |
| resolve alert marks it resolved and updates timestamp | PATCH …/resolve | 200 + resolved=true + resolvedAt set |
| delete alert returns 204 | DELETE …/004 | 204 |
| delete alert with mitigations cascades | delete alert with 2 mitigations | 204 + mitigations empty |
| list alerts returns 403 without authentication | no token | 403 |

---

## Score — `ScoreIntegrationTest` (5 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| get score returns calculated score for user with alerts | user1 with unresolved alerts | 200 + score in [0,100] |
| get score returns 100 for user with no unresolved alerts | user2 with no alerts | 200 + score=100 |
| score history returns paginated records | user1 has score history | 200 + non-empty content |
| score recalculates after alert resolution | resolve CRITICAL alert | recalculated score ≥ pre-resolve score |
| get score returns 403 without authentication | no token | 403 |

---

## Mitigations — `MitigationIntegrationTest` (5 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| list mitigations returns all for authenticated user | user1 has 2 mitigations | 200 + array of 2 |
| get mitigations by alert id | mitigations for alert aaaa1111-…-001 | 200 + array of 2, all fields present |
| complete mitigation sets completed flag and timestamp | PATCH …/complete | 200 + completed=true + completedAt set |
| complete mitigation returns 404 for unknown id | random UUID | 404 |
| list mitigations returns 403 without auth | no token | 403 |

---

## Scans — `ScanIntegrationTest` (8 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| breach scan creates alerts from scanner results | mock returns 1 breach | 200 + array with DATA_BREACH alert |
| breach scan persists scan result with jsonb findings | mock returns breach | admin scan history shows structured findings |
| identity scan creates identity exposure alerts | mock returns identity result | 200 + IDENTITY_EXPOSURE alert |
| pii scan creates tracker exposure alerts | mock returns PII result | 200 + TRACKER_EXPOSURE alert |
| social scan creates social footprint alerts | mock returns social result | 200 + SOCIAL_FOOTPRINT alert |
| full scan aggregates all four scan types | all mocks return 1 result | 200 + ≥4 alerts |
| scan recalculates score after creating alerts | breach scan on user with score=100 | score < 100 after scan |
| breach scan returns 403 without auth | no token | 403 |

---

## Admin — `AdminIntegrationTest` (14 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| create user returns 201 | POST /admin/users with new email | 201 + user object |
| create user returns 409 for duplicate email | email already exists | 409 |
| list users returns paginated results with scores | 5 users in DB | 200 + totalElements=5 |
| get user detail returns user with alert count and recent alerts | user1 detail | 200 + alertCount=6 |
| get user returns 404 for unknown id | random UUID | 404 |
| delete user cascades all related data | delete user1 | 204 + subsequent GET returns 404 |
| get user alerts returns paginated list | user1's alerts | 200 + totalElements=6 |
| trigger scan returns 202 with job id | POST …/scan | 202 + jobId |
| poll scan job until completed | trigger + poll | status=COMPLETED within 15s |
| failed scan records friendly error message | scanner throws RuntimeException | status=FAILED + errorMessage="Scan could not be completed. Please try again." |
| scan history returns results with structured findings | scan_results fixtures | 200 + details array present |
| get stats returns system wide counts | 5 users, 8 alerts in DB | totalUsers=5 + totalAlerts=8 |
| admin endpoints return 403 with regular user jwt | JWT without ROLE_ADMIN | 403 |
| admin endpoints return 403 with no token | no token | 403 |

---

## Permission Audit — `PermissionAuditIntegrationTest` (3 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| submit risky permissions creates alerts | `android.permission.CAMERA` + `ACCESS_FINE_LOCATION` | 200 + ≥1 APP_OVERPERMISSIONS alert |
| submit safe permissions creates no alerts | `android.permission.INTERNET` | 200 + empty array |
| permission audit returns 403 without auth | no token | 403 |

---

## Security — `SecurityIntegrationTest` (5 tests)

| Test | Scenario | Expected |
|------|----------|----------|
| unauthenticated requests to protected endpoints return 403 | GET /alerts, /score, POST /scan/breach | all 403 |
| malformed jwt returns 403 | `Authorization: Bearer not-a-jwt` | 403 |
| admin token grants access to admin endpoints | `Authorization: Bearer test-admin-token` | 200 |
| regular user jwt cannot access admin endpoints | valid JWT to /admin/users | 403 |
| auth endpoints are public | POST /auth/login with bad credentials | 401 (not 403) |

---

## Summary

| Class | Tests | Status |
|-------|-------|--------|
| AuthIntegrationTest | 11 | ✅ PASS |
| AlertIntegrationTest | 10 | ✅ PASS |
| ScoreIntegrationTest | 5 | ✅ PASS |
| MitigationIntegrationTest | 5 | ✅ PASS |
| ScanIntegrationTest | 8 | ✅ PASS |
| AdminIntegrationTest | 14 | ✅ PASS |
| PermissionAuditIntegrationTest | 3 | ✅ PASS |
| SecurityIntegrationTest | 5 | ✅ PASS |
| **Total** | **61** | ✅ **ALL PASS** |

> Last run: 2026-04-02 · Branch: `test/api-integration-tests` · PR: gm-solvd/Alerts#13
