---
paths:
  - "api/**/*Test.kt"
  - "api/**/test/**"
---

# Testing — API

## Test Pyramid

- **Unit** (services): MockK, no Spring context, instantiate classes directly
- **Integration** (HTTP→DB): `@SpringBootTest` + Testcontainers PostgreSQL singleton
- **E2E**: Minimal, happy-path only

## Unit Test Pattern

```kotlin
class AlertServiceTest {
    private val alertRepository = mockk<AlertRepository>()
    private val service = AlertService(alertRepository)

    @Test
    fun `resolveAlert updates resolved flag`() { ... }
}
```

## Integration Test Pattern

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureMockMvc`
- Singleton Testcontainers PostgreSQL (start once, never stop)
- **No `@Transactional`** — tests see real commit behavior
- `@Sql` for test fixtures, cleanup after each test
- `@MockkBean` for external services only

## Controller Slice Tests

- `@WebMvcTest` + `@Import(SecurityConfig::class, JwtAuthFilter::class)`
- `@MockkBean` for all service dependencies

## Naming

`` `<action> <condition> <expected outcome>` `` using backtick-quoted method names.

## Coverage

- 80%+ for services, all endpoints covered, at least one unhappy path per endpoint.

Full reference: `rules/architecture-api/testing.md`
