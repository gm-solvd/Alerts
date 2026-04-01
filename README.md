# Privacy Alert System

A mobile-first privacy monitoring application that tracks digital exposure, calculates a privacy health score, and sends categorized threat alerts with actionable mitigations.

## What it does

- **Scans** for data breaches, PII exposure, identity aggregation, and social footprint across public sources
- **Scores** your privacy health using a CVSS-inspired deduction model (0–100, with diminishing returns per category)
- **Alerts** you to threats categorized by type and severity, with step-by-step mitigations
- **Audits** Android app permissions for over-permissioned apps

## Repository Structure

```
Alerts/
├── api/        Kotlin + Spring Boot REST API
├── admin/      Vue 3 admin dashboard
├── mobile/     KMP + Compose Multiplatform app (not yet started)
├── rules/      Architecture decisions and workflow standards
└── .claude/    Claude Code skills and automation
```

## Getting Started

### API

> Requires Java 21 and Docker.

```bash
cd api
docker-compose up -d        # start PostgreSQL
./gradlew bootRun           # start API on :8080
```

### Admin Dashboard

```bash
cd admin
npm install
npm run dev                 # start dev server on :5173 (proxies /api to :8080)
```

---

## Documentation

### Architecture

| Document | Description |
|----------|-------------|
| [Clean Architecture — API](rules/architecture-api/clean-architecture.md) | Layer structure, dependency rules, package layout |
| [Auth](rules/architecture-api/auth.md) | JWT strategy, token refresh, OAuth flow |
| [API Conventions](rules/architecture-api/api.md) | REST endpoint conventions, DTOs, versioning, pagination |
| [Scoring Algorithm](rules/architecture-api/scoring.md) | CVSS-inspired privacy health score formula |
| [Error Handling](rules/architecture-api/error-handling.md) | Exception hierarchy, HTTP status mapping |
| [Database](rules/architecture-api/database.md) | Flyway migrations, JPA entity conventions |
| [Testing](rules/architecture-api/testing.md) | Unit, controller slice, and integration test patterns |
| [DI Patterns](rules/architecture-api/di.md) | Dependency injection and `@Primary` conventions |
| [Mobile Architecture](rules/architecture-mobile/clean-architecture.md) | KMP architecture rules (future) |

### Workflow

| Document | Description |
|----------|-------------|
| [Implementation Workflow](rules/general/implementation-workflow.md) | 9-step feature/fix process with STOP checkpoints |
| [Commit Format](rules/general/commits.md) | Commit message types, scopes, and rules |
| [PR Guidelines](rules/general/pr-guidelines.md) | Pull request process and CI validation |
| [Progress Tracker](rules/general/progress.md) | MVP feature completion status |

### Codebase Summaries

| Directory | Summary |
|-----------|---------|
| [api/](api/SUMMARY.md) | API module build commands and dependencies |
| [api/…/domain/](api/src/main/kotlin/com/privacyalert/domain/SUMMARY.md) | Domain layer overview |
| [api/…/domain/model/](api/src/main/kotlin/com/privacyalert/domain/model/SUMMARY.md) | All domain models |
| [api/…/domain/service/](api/src/main/kotlin/com/privacyalert/domain/service/SUMMARY.md) | Business services and scoring algorithm |
| [api/…/domain/repository/](api/src/main/kotlin/com/privacyalert/domain/repository/SUMMARY.md) | Repository interface contracts |
| [api/…/data/](api/src/main/kotlin/com/privacyalert/data/SUMMARY.md) | Data layer overview |
| [api/…/data/entity/](api/src/main/kotlin/com/privacyalert/data/entity/SUMMARY.md) | JPA entities |
| [api/…/data/repository/](api/src/main/kotlin/com/privacyalert/data/repository/SUMMARY.md) | JPA repositories and adapters |
| [api/…/api/](api/src/main/kotlin/com/privacyalert/api/SUMMARY.md) | API layer overview and request flow |
| [api/…/api/controller/](api/src/main/kotlin/com/privacyalert/api/controller/SUMMARY.md) | All REST endpoints |
| [api/…/api/dto/](api/src/main/kotlin/com/privacyalert/api/dto/SUMMARY.md) | Request/response DTOs |
| [api/…/integration/](api/src/main/kotlin/com/privacyalert/integration/SUMMARY.md) | External scanner clients |
| [api/…/integration/scraping/](api/src/main/kotlin/com/privacyalert/integration/scraping/SUMMARY.md) | Rate-limited HTTP and robots.txt checker |
| [api/…/config/](api/src/main/kotlin/com/privacyalert/config/SUMMARY.md) | Security config, JWT filter, app properties |
| [api/src/test/](api/src/test/SUMMARY.md) | Test suite structure and patterns |
| [admin/](admin/SUMMARY.md) | Admin dashboard build and structure |
| [admin/src/](admin/src/SUMMARY.md) | Vue app source, router, API client |
| [admin/src/components/](admin/src/components/SUMMARY.md) | Reusable Vue components |
| [admin/src/views/](admin/src/views/SUMMARY.md) | Admin page views |
| [rules/](rules/SUMMARY.md) | Rules directory overview |
| [rules/general/](rules/general/SUMMARY.md) | Workflow and commit rules |
| [rules/architecture-api/](rules/architecture-api/SUMMARY.md) | API architecture rule files |
| [rules/architecture-mobile/](rules/architecture-mobile/SUMMARY.md) | Mobile architecture rule files |
| [.claude/skills/](.claude/skills/SUMMARY.md) | Claude Code automation skills |

### CI / CD

| File | Description |
|------|-------------|
| [.github/workflows/pr-validation.yml](.github/workflows/pr-validation.yml) | Runs compile, lint, and test on every PR |
| [.github/workflows/pr-autofix.yml](.github/workflows/pr-autofix.yml) | Auto-fixes lint/format issues when CI fails |
