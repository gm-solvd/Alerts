# Test Suite

Mirrors the main source structure. Three test categories with distinct tooling.

## Test Categories

### Unit Tests — `domain/service/`
Pure business logic tests. No Spring context. Classes instantiated directly with MockK mocks.

| File | Tests |
|------|-------|
| `AlertServiceTest` | List/filter, resolve (updates score), delete |
| `AuthServiceTest` | Register (duplicate email), login (wrong password), OAuth upsert, token refresh/rotation |
| `ScanServiceTest` | Each scan type creates correct alerts and persists ScanResult |
| `ScoreServiceTest` | Score deduction formula, diminishing returns, min 0 |
| `MitigationServiceTest` | Find by alert/user, mark complete |
| `PermissionAuditServiceTest` | Risky permission detection, alert creation |

### Controller Slice Tests — `api/controller/`
`@WebMvcTest` + `@Import(SecurityConfig, JwtAuthFilter)` + `@MockkBean` for services. Tests HTTP contract.

| File | Tests |
|------|-------|
| `AlertControllerTest` | List pagination, filter by category/severity, resolve, delete — auth required |
| `AuthControllerTest` | Register/login 200s, duplicate email 409, bad credentials 401 |
| `ScanControllerTest` | Each scan endpoint, unauthenticated 401 |
| `ScoreControllerTest` | Current score, history pagination |
| `MitigationControllerTest` | Find, complete |
| `PermissionAuditControllerTest` | Submit permissions list |

### Integration Tests — `integration/`
Real HTTP calls against WireMock stubs (never real external APIs). Tests scanner implementations.

| File | Tests |
|------|-------|
| `LocalBreachScannerImplTest` | DB query by email/phone — uses Testcontainers |
| `PasteMonitorClientTest` | WireMock paste site responses |
| `PiiExposureScannerImplTest` | WireMock people-finder responses |
| `SocialFootprintScannerImplTest` | WireMock social platform responses |
| `CompositeBreachScannerTest` | Deduplication logic across all sub-scanners |

### Config Tests — `config/`

| File | Tests |
|------|-------|
| `JwtAuthFilterTest` | Valid token sets SecurityContext, missing/expired token returns 401 |
| `JwtProviderImplTest` | Token generation, validation, expiry |

### Entity Tests — `data/entity/`

| File | Tests |
|------|-------|
| `EntityPersistableTest` | Regression: entities with pre-set UUIDs perform INSERT not SELECT+UPDATE |
