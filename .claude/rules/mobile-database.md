---
paths:
  - "mobile/**/*.sq"
  - "mobile/**/sqldelight/**"
  - "mobile/**/local/**"
---

# Database — Mobile (SQLDelight)

## Stack

SQLite via SQLDelight. Single schema file, platform drivers via `expect/actual`.

## Schema

- File: `shared/src/commonMain/sqldelight/com/privacyalert/db/PrivacyAlert.sq`
- Database class: `PrivacyAlertDb` (package `com.privacyalert.db`)
- Drivers: `AndroidSqliteDriver` (Android), `NativeSqliteDriver` (iOS) — registered in `PlatformModule`

## Migration Policy

**Pre-release (no production users yet):** No migrations needed. Edit `PrivacyAlert.sq` directly — the database is recreated on fresh install. Do NOT create migration files unless explicitly asked.

**When to ask:** Before any schema change (new table, new column, altered column), ask the user:
> "Should I create a SQLDelight migration for this change, or just edit the schema directly?"

This ensures the policy is re-evaluated as the app approaches release.

## Conventions

- All tables and queries live in a single `PrivacyAlert.sq` file
- Use named queries (e.g., `getTokens:`, `upsertTokens:`)
- Use `INSERT OR REPLACE` for upsert patterns
- Data access is encapsulated in `data/local/` classes (e.g., `SqlDelightTokenStorage`)
- Domain layer uses repository interfaces — never imports SQLDelight directly
- DI: `single` scope for `PrivacyAlertDb` and storage implementations

Full reference: `rules/architecture-mobile/`
