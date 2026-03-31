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

## Integration Tests (Controllers + DB)

- Use `@SpringBootTest` + `@AutoConfigureMockMvc`
- Use **Testcontainers** for a real PostgreSQL instance
- Annotate with `@Transactional` to roll back after each test

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun `GET api-alerts returns 200 with alert list`() {
        mockMvc.get("/api/alerts") {
            header("Authorization", "Bearer $testJwt")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].severity") { exists() }
        }
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
