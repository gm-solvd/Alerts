# Dependency Injection — API (Spring)

## Approach
Use **constructor injection** exclusively. No field injection (`@Autowired` on fields), no setter injection.

---

## Rules

```kotlin
// ✅ Correct — constructor injection
@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val scoreService: ScoreService
) { ... }

// ❌ Wrong — field injection
@Service
class AlertService {
    @Autowired
    private lateinit var alertRepository: AlertRepository
}
```

- Mark all injected properties `private val`
- Domain services depend only on **interfaces** (defined in `domain/repository/`), never on concrete JPA repos
- Config classes use `@Configuration` + `@Bean` methods for external clients (Ktor, HIBP client, etc.)

---

## Bean Registration by Layer

| Layer | Annotation |
|---|---|
| Domain services | `@Service` |
| Controllers | `@RestController` |
| Data repositories | `@Repository` (or auto via `JpaRepository`) |
| External clients | `@Bean` in a `@Configuration` class |
| Config | `@Configuration` |

---

## External Client Example

```kotlin
@Configuration
class IntegrationConfig {

    @Bean
    fun hibpClient(
        @Value("\${hibp.api-key}") apiKey: String
    ): HibpClient = HibpClient(apiKey)
}
```

---

## Testing with DI
- Use `@SpringBootTest` for full integration tests
- Use `@WebMvcTest` for controller-layer tests (mock services with `@MockBean`)
- Use plain constructor calls with mock objects (MockK) for unit tests on services — no Spring context needed
