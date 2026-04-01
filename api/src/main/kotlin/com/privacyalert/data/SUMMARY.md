# Data Layer

Spring Data JPA implementation of the domain repository interfaces. This layer owns all database concerns: entities, ORM mappings, and the adapter pattern that bridges JPA to the domain.

## Sub-directories

| Directory | Purpose |
|-----------|---------|
| [`entity/`](entity/SUMMARY.md) | JPA `@Entity` classes with `toDomain()` / `toEntity()` mappers |
| [`repository/`](repository/SUMMARY.md) | `*JpaRepository` interfaces + `*RepositoryAdapter` implementations of domain interfaces |

## Database Migrations

Flyway migrations live in `api/src/main/resources/db/migration/`. Files follow `V<n>__<description>.sql`. Never modify existing migrations — add new ones only.

## Dependency Rule

```
data/ ──► domain/   (implements domain interfaces)
data/ ──► Spring Data JPA, Flyway
```

The domain layer knows nothing about entities or JPA types.
