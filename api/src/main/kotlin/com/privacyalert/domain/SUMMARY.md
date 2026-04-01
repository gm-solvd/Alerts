# Domain Layer

The core business layer of the Privacy Alert System. Contains zero Spring or JPA dependencies — pure Kotlin only. This layer defines what the system does; other layers define how it's stored or delivered.

## Sub-directories

| Directory | Purpose |
|-----------|---------|
| [`model/`](model/SUMMARY.md) | Data classes: `Alert`, `User`, `ScanResult`, `ScoreRecord`, `Severity`, `ThreatCategory`, exceptions, etc. |
| [`service/`](service/SUMMARY.md) | Business logic: `AlertService`, `ScanService`, `ScoreService`, `AuthService`, `AdminService`, `PermissionAuditService`, `MitigationService` + scanner/provider interfaces |
| [`repository/`](repository/SUMMARY.md) | Persistence interfaces only — no implementations here |

## Dependency Rule

```
domain/ ──► (nothing — no external dependencies)
data/   ──► domain/
api/    ──► domain/
```

Domain models flow up; frameworks are injected from outside via interfaces.

## Entry Point

`PrivacyAlertApplication.kt` — Spring Boot `@SpringBootApplication` main class, one level above this directory.
