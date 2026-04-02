# Testing — API

## Test Pyramid

```
        [E2E]           ← minimal, happy-path only
      [Integration]     ← controllers + DB (Testcontainers)
    [Unit]              ← services, mappers (MockK, no Spring context)
```

---

## Unit Tests (Services)

- No Spring context — instantiate classes directly
- Mock dependencies with **MockK**
- Test: happy path, not-found, validation errors, external service failure

```kotlin
class AlertServiceTest {
    private val alertRepository = mockk<AlertRepository>()
    private val scoreService = mockk<ScoreService>()
    private val service = AlertService(alertRepository, scoreService)

    @Test
    fun `resolveAlert updates resolved flag and recalculates score`() {
        // given
        val alert = Alert(id = UUID.randomUUID(), resolved = false, ...)
        every { alertRepository.findById(alert.id) } returns alert
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(alert.userId) } just Runs

        // when
        service.resolveAlert(alert.id)

        // then
        verify { alertRepository.save(match { it.resolved }) }
        verify { scoreService.recalculate(alert.userId) }
    }
}
```

---

## Integration Tests (Full Stack — HTTP → Controller → DB)

- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` — tests real Tomcat and the full security filter chain
- Use **Testcontainers** singleton pattern: start the container once in a static initializer (`also { it.start() }`), never stop it between test classes — prevents Spring context cache invalidation
- **Do NOT use `@Transactional`** — it does not work with `RANDOM_PORT` (HTTP requests run in a different thread from the test context)
- Use `@Sql` for fixtures: `BEFORE_TEST_METHOD` to seed data, `AFTER_TEST_METHOD` for cleanup
- `@MockkBean` only for external scanner interfaces; all other beans run real
- All integration tests extend `BaseIntegrationTest`

### Infrastructure files

| File | Purpose |
|------|---------|
| `src/test/kotlin/.../integration/BaseIntegrationTest.kt` | Singleton PostgreSQL container, `@MockkBean` for scanners, HTTP helpers |
| `src/test/resources/application-integration.yml` | Overrides H2 driver with PostgreSQL, enables Flyway |
| `src/test/resources/sql/cleanup.sql` | TRUNCATE all tables in FK-safe order |
| `src/test/resources/sql/common-fixtures.sql` | 2 users with known bcrypt hashes |
| `src/test/resources/sql/alerts-fixtures.sql` | 6 alerts + mitigations + score history for user1 |
| `src/test/resources/sql/scan-fixtures.sql` | scan_results with JSONB findings |
| `src/test/resources/sql/admin-fixtures.sql` | 3 extra users + alerts for admin/stats tests |

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
abstract class BaseIntegrationTest {
    companion object {
        @JvmStatic
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16").also { it.start() }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired lateinit var restTemplate: TestRestTemplate
    @MockkBean lateinit var breachScanner: BreachScanner
    // ... other external scanner mocks
}

@Sql(scripts = ["/sql/cleanup.sql", "/sql/common-fixtures.sql", "/sql/alerts-fixtures.sql"],
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = ["/sql/cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AlertIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `resolve alert marks it resolved and updates timestamp`() {
        val tokens = loginUser("user@test.com", "password123")
        val response = patch(
            "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000001/resolve",
            userHeaders(tokens.accessToken),
            String::class.java,
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
```

---

## External API Tests

- Use **WireMock** to stub HIBP and OAuth endpoints
- Never call real external APIs in tests

---

## Naming Convention
```
`<method/action> <condition> <expected outcome>`

resolveAlert updates resolved flag and recalculates score
GET api-alerts returns 200 with alert list
findById throws ResourceNotFoundException when id does not exist
```

---

## Coverage Targets
- Services: 80%+ line coverage
- Controllers: all endpoints covered by at least one integration test
- Integration scenarios: at least one unhappy path per endpoint

---

## Linting

- **Always run `./gradlew ktlintFormat` before committing** to auto-fix formatting issues
- Run `./gradlew ktlintCheck` to verify — this is what CI runs
- Common violations: import ordering, multiline expression formatting, parameter newlines, class body blank lines, max line length (140 chars)
- If `ktlintFormat` cannot auto-correct an issue (e.g., line too long), manually refactor the line
