---
paths:
  - "api/**/data/**"
  - "api/**/entity/**"
  - "api/**/migration/**"
---

# Database — API

## Stack

PostgreSQL via Spring Data JPA + Hibernate. Flyway for migrations.

## Schema (core tables)

- `users` — UUID pk, email (unique), password_hash (nullable for OAuth), display_name, auth_provider, timestamps
- `alerts` — UUID pk, user_id FK, title, description, severity, category, source, resolved, timestamps
- `score_history` — UUID pk, user_id FK, score (0-100), breakdown (JSONB), calculated_at
- `mitigations` — UUID pk, alert_id FK, action, status (PENDING/IN_PROGRESS/DONE), timestamps
- `refresh_tokens` — UUID pk, user_id FK, token_hash, expires_at, timestamps

## Entity Conventions

- UUID primary keys (`@Id @GeneratedValue`)
- `Instant` for timestamps (maps to `TIMESTAMP WITH TIME ZONE`)
- No `@Column(name=...)` unless column name differs from field name
- `toEntity()` / `toDomain()` mapper extension functions in entity files

## Migration Rules

- Files in `src/main/resources/db/migration/V<n>__<desc>.sql`
- **Never modify existing migrations** — always create new ones
- Each migration is self-contained and idempotent where possible

## Repository Pattern

- Domain layer defines interfaces (`AlertRepository`, `UserRepository`)
- Data layer implements via `RepositoryAdapter` wrapping `JpaRepository`
- Adapters convert between domain models and JPA entities

Full reference: `rules/architecture-api/database.md`
