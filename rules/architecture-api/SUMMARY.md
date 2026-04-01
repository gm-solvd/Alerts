# API Architecture Rules

Detailed rules enforcing Clean Architecture patterns in the Spring Boot API.

## Files

| File | Description |
|------|-------------|
| `clean-architecture.md` | Layer structure and strict dependency rules. Domain → no dependencies. Data/API/Integration → domain only. Config → Spring. Includes package layout, mapper conventions, and a complete request-flow example |
| `api.md` | REST endpoint conventions: versioning (`/api/v1/`), HTTP methods, response codes, DTO requirements, pagination format, `PageResponse<T>` usage |
| `auth.md` | JWT strategy: HS256, 15-min access tokens, 30-day refresh tokens with rotation (revoke on use). `authenticatedUserId()` helper. OAuth flow. `SecurityContext` usage |
| `database.md` | Flyway migration rules (never modify existing files, naming convention `V<n>__<desc>.sql`), JPA entity conventions, `Persistable<UUID>` requirement, transaction boundaries |
| `di.md` | Dependency injection patterns: constructor injection only, `@Primary` for composite implementations, interface-based DI to keep domain free of Spring |
| `scoring.md` | CVSS-inspired privacy health score algorithm. Penalty = `severity.points × category.weight × (1 + ln(alertCount))`. Score = `max(0, 100 - Σpenalties)`. Category weights and severity point tables |
| `error-handling.md` | Exception hierarchy (`AppException` subtypes), `@ControllerAdvice` global handler, HTTP status mapping table, no exception catching in controllers |
| `testing.md` | Test patterns: unit (MockK, no Spring), controller slice (`@WebMvcTest` + `@MockkBean`), integration (`@SpringBootTest` + Testcontainers), external API stubs (WireMock). Coverage targets: 80%+ services, all endpoints |
