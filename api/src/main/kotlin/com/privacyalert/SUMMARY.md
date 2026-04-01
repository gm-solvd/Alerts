# Privacy Alert API — Kotlin Source Root

Spring Boot application implementing the Privacy Alert System backend. Follows strict Clean Architecture with four layers.

## Layer Map

| Directory | Layer | Framework | Description |
|-----------|-------|-----------|-------------|
| [`domain/`](domain/SUMMARY.md) | Domain | Pure Kotlin | Models, business services, repository interfaces, scanner interfaces |
| [`data/`](data/SUMMARY.md) | Infrastructure | Spring Data JPA | JPA entities, repository adapters, database migrations |
| [`api/`](api/SUMMARY.md) | Presentation | Spring Web MVC | REST controllers, DTOs, global error handler |
| [`integration/`](integration/SUMMARY.md) | Infrastructure | Spring HTTP | External scanners (HIBP, paste monitor, identity, PII, social) |
| [`config/`](config/SUMMARY.md) | Configuration | Spring Security | JWT auth filter, security rules, app properties |

## Entry Point

`PrivacyAlertApplication.kt` — `@SpringBootApplication` with `main()`.

## Dependency Direction

```
config/ ──► domain/ + Spring Security
api/    ──► domain/ + Spring Web
data/   ──► domain/ + Spring Data JPA
integration/ ──► domain/ + HTTP clients
domain/ ──► (nothing external)
```
