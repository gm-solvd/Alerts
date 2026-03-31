# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Privacy Alert System — a mobile-first app that monitors digital exposure, scores privacy health, and sends categorized threat alerts with actionable mitigations. Monorepo with `api/` (Kotlin + Spring Boot) and `mobile/` (KMP + Compose Multiplatform, not yet started).

## Build & Development Commands

All API commands run from the `api/` directory. Java 21 is required (on macOS: `export JAVA_HOME=/opt/homebrew/opt/openjdk@21`).

```bash
# Start local PostgreSQL
docker-compose up -d

# Run API
./gradlew bootRun

# Compile only
./gradlew compileKotlin

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.privacyalert.domain.service.AlertServiceTest"

# Run a single test method
./gradlew test --tests "com.privacyalert.domain.service.AlertServiceTest.resolveAlert updates resolved flag"

# Lint check
./gradlew ktlintCheck
```

## Architecture

Clean Architecture with strict layer isolation. The dependency rule: each layer depends only on layers below it. Domain has **zero** Spring/framework dependencies.

```
api/src/main/kotlin/com/privacyalert/
├── domain/           # Pure Kotlin — no Spring, no JPA
│   ├── model/        # Data classes (Alert, User, UserScanProfile, etc.)
│   ├── service/      # Business logic + scanner interfaces (BreachScanner, PiiExposureScanner, etc.)
│   └── repository/   # Interfaces only — implemented in data/
├── data/             # Spring Data JPA
│   ├── entity/       # JPA @Entity classes with toEntity()/toDomain() mappers
│   └── repository/   # JpaRepository interfaces + RepositoryAdapter implementations
├── api/              # Spring Web — thin controllers
│   ├── controller/   # @RestController — parse input, call service, return DTO
│   └── dto/          # Request/Response objects (never expose domain models)
├── integration/      # External clients (HIBP, breach scanners, web scrapers)
│   └── scraping/     # Rate-limited HTTP client, robots.txt compliance
└── config/           # Spring @Configuration, security, JWT, properties
```

**Key constraint**: Domain services receive/return domain models only. DTOs stay in `api/`, entities stay in `data/`. Controllers are thin — no business logic.

## Implementation Workflow

**Every feature/fix must follow these steps with STOP checkpoints:**

1. **Task Intake** — Summarize understanding. **STOP** for confirmation.
2. **Branch Creation** — `git checkout develop && git checkout -b <type>/<scope>-<description>`. **STOP** for approval.
3. **Planning** — Create implementation plan. **STOP** for approval.
4. **Implementation** — Implement step by step. **NO COMMITS** yet.
5. **Validation** — Run `compileKotlin`, `ktlintFormat`, `ktlintCheck`, `test`. **STOP** to report results.
6. **Human Review** — Show `git diff`, wait for user review.
7. **Atomic Commits** — Group changes logically. Stage files by name (never `git add .`).
8. **Push & Pull Request** — Push branch, draft PR, **STOP** for approval, then `gh pr create --base develop`. Return PR URL. CI validates automatically. If CI fails, Claude auto-fixes via `pr-autofix.yml`. PR is not auto-merged.
9. **Update Progress** — Update `rules/general/progress.md`.

## Branching & Commits

```
main     → production-ready, protected
develop  → integration branch
feat/*   → off develop     fix/*    → off develop (or main for hotfixes)
```

Branch naming: `<type>/<scope>-<short-description>` (e.g., `feat/scan-pii-exposure`).

Commit format: `<type>(<scope>): <description>` — imperative mood, max 72 chars, no period.
Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`, `perf`.
Scopes: `mobile`, `android`, `ios`, `api`, `db`, `auth`, `alerts`, `score`, `scan`.

## Testing

- **Unit tests**: MockK, no Spring context, instantiate classes directly
- **Controller slice tests**: `@WebMvcTest` + `@Import(SecurityConfig::class, JwtAuthFilter::class)`, `@MockkBean` for dependencies
- **Integration tests**: `@SpringBootTest` + Testcontainers PostgreSQL
- **External APIs**: WireMock stubs, never call real APIs
- Test naming: `` `<action> <condition> <expected outcome>` `` using backtick-quoted method names
- Coverage targets: 80%+ for services, all endpoints covered

## Key Technical Details

- **Authentication**: JWT (HS256) — 15min access tokens, 30-day refresh tokens. `authenticatedUserId()` extracts UUID from SecurityContext.
- **Scoring**: CVSS-inspired deduction model. Score = max(0, 100 - penalties). Penalties use severity weights × category weights × logarithmic decay `(1 + ln(n))`.
- **Scanning**: CompositeBreachScanner (@Primary) merges LocalBreachScanner + PasteMonitor + optional HIBP. Separate scanners for PII exposure, identity, social footprint.
- **HIBP**: Opt-in only via `app.hibp.enabled=true`. Disabled by default.
- **Error handling**: `@ControllerAdvice` global handler. Services throw domain exceptions (`ResourceNotFoundException`, `ConflictException`, etc.). Controllers never catch exceptions.
- **Migrations**: Flyway, files in `src/main/resources/db/migration/V<n>__<desc>.sql`. Never modify existing migrations.

## Rules Reference

Detailed architecture, convention, and workflow rules are in `rules/`:
- `rules/general/` — workflow, commits, PRs, progress tracking
- `rules/architecture-api/` — clean architecture, API conventions, database, auth, scoring, testing, error handling, DI
- `rules/architecture-mobile/` — KMP architecture, components, navigation, styling, testing

Custom automation skills are in `.claude/skills/` — `workflow`, `branch`, `commit`, `pr`, `validate`, `test`, `lint`, `push`, `implement`, `progress`.
