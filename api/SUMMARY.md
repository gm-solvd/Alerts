# API Module

Kotlin + Spring Boot backend for the Privacy Alert System. Java 21 required.

## Directory Structure

| Path | Purpose |
|------|---------|
| `src/main/kotlin/` | [Application source](src/main/kotlin/com/privacyalert/SUMMARY.md) — domain, data, api, integration, config layers |
| `src/test/kotlin/` | [Test suite](src/test/SUMMARY.md) — unit, controller slice, and integration tests |
| `src/main/resources/db/migration/` | Flyway SQL migrations (`V<n>__<desc>.sql`) — never modify existing files |
| `src/main/resources/application.yml` | App configuration (JWT secrets, HIBP settings, DB connection) |
| `docker-compose.yml` | Local PostgreSQL for development |
| `build.gradle.kts` | Gradle build: Kotlin, Spring Boot, JJWT, MockK, Testcontainers |

## Build Commands

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21  # macOS
docker-compose up -d                            # start PostgreSQL
./gradlew bootRun                              # run API
./gradlew compileKotlin                        # compile only
./gradlew test                                 # all tests
./gradlew ktlintCheck                          # lint
./gradlew ktlintFormat                         # auto-format
```

## Key Dependencies

- Spring Boot 3.x (Web, Security, Data JPA, Validation)
- Kotlin coroutines (for parallel scan execution)
- JJWT (JWT generation/validation)
- Flyway (database migrations)
- PostgreSQL (via Docker locally, Testcontainers in tests)
- MockK (Kotlin-idiomatic mocking)
- WireMock (HTTP stub server for integration tests)
