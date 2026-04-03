# Privacy Alert System

A mobile-first app that monitors your digital exposure: it scans for data breaches and PII leaks, calculates a privacy health score, and delivers categorized alerts with actionable mitigations. Built as a monorepo with a Kotlin/Spring Boot API and a Kotlin Multiplatform mobile app (iOS + Android).

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Privacy Alert System                       │
├────────────────────┬─────────────────────────────────────────┤
│   Admin (Vue 3)    │         Mobile (KMP)                    │
│   admin/           │         mobile/          [not started]  │
└────────┬───────────┴────────────────┬────────────────────────┘
         │ HTTP /api/v1/admin         │ HTTP /api/v1/*
         ▼                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  Spring Boot API  (api/)                      │
│                                                              │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────────┐  │
│  │  api/       │   │  domain/     │   │  integration/    │  │
│  │  controllers│──▶│  services    │──▶│  HIBP · XON      │  │
│  │  DTOs       │   │  models      │   │  scrapers        │  │
│  └─────────────┘   │  repos (i/f) │   └──────────────────┘  │
│                    └──────┬───────┘   ┌──────────────────┐  │
│                           │           │  data/           │  │
│                           └──────────▶│  JPA entities    │  │
│                                       │  repo adapters   │  │
│                                       └────────┬─────────┘  │
└────────────────────────────────────────────────┼────────────┘
                                                 ▼
                                    ┌────────────────────────┐
                                    │  PostgreSQL  (Docker)  │
                                    └────────────────────────┘

Dependency rule: each layer depends only on layers below it.
Domain layer has zero Spring/framework dependencies.
```

---

## Repo Layout

```
.
├── api/          Kotlin + Spring Boot REST API
├── admin/        Vue 3 admin dashboard
├── mobile/       KMP + Compose Multiplatform (not started)
├── rules/        Architecture & workflow standards
│   ├── general/          Workflow, commits, PRs, progress
│   ├── architecture-api/ Clean arch, auth, scoring, testing
│   └── architecture-mobile/ KMP architecture & components
└── .claude/      Claude Code skills and automation
    └── skills/   /workflow, /branch, /commit, /pr, /validate …
```

All directories have a `SUMMARY.md` describing their files. Key links:

| Area | Summary | Rules |
|------|---------|-------|
| API domain | [api/src/…/domain/SUMMARY.md](api/src/main/kotlin/com/privacyalert/domain/SUMMARY.md) | [architecture-api/](rules/architecture-api/) |
| API data | [api/src/…/data/SUMMARY.md](api/src/main/kotlin/com/privacyalert/data/SUMMARY.md) | [architecture-api/](rules/architecture-api/) |
| API controllers | [api/src/…/api/SUMMARY.md](api/src/main/kotlin/com/privacyalert/api/SUMMARY.md) | [architecture-api/](rules/architecture-api/) |
| Admin frontend | [admin/src/SUMMARY.md](admin/src/SUMMARY.md) | — |
| General workflow | — | [rules/general/workflow.md](rules/general/workflow.md) |
| Commits | — | [rules/general/commits.md](rules/general/commits.md) |
| Pull requests | — | [rules/general/pr-guidelines.md](rules/general/pr-guidelines.md) |
| Progress tracker | — | [rules/general/progress.md](rules/general/progress.md) |

---

## Getting Started

### Prerequisites

- Docker (for PostgreSQL)
- Java 21 — on macOS: `export JAVA_HOME=/opt/homebrew/opt/openjdk@21`
- Node 18+ (for admin dashboard)

### 1. Start the database

```bash
docker-compose up -d
```

### 2. Run the API

```bash
cd api
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
./gradlew bootRun
# API available at http://localhost:8080
```

### 3. Run the admin dashboard

```bash
cd admin
npm install
npm run dev
# Dashboard available at http://localhost:5173
```

### Environment variables

Copy `.env.example` to `.env` and fill in:

```
DB_URL=jdbc:postgresql://localhost:5432/privacyalert
DB_USER=dev
DB_PASSWORD=dev
JWT_SECRET=<generate a secure random string>
ADMIN_TOKEN=changeme
```

---

## Branching & Workflow

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready, protected |
| `develop` | Integration branch — all PRs merge here |
| `feat/*` | New features, branched off `develop` |
| `fix/*` | Bug fixes, branched off `develop` (or `main` for hotfixes) |

> **Never commit directly to `develop` or `main`.** All work happens on feature/fix branches, merged via PR.

Every **feature** is split into **tasks** by workstream (web backend, web frontend, mobile UI, mobile business logic). Related tasks use a parent/child branch pattern — children merge into the parent, then the parent merges into develop. See [workflow rules](rules/general/workflow.md) for the full step-by-step process.

---

## Key Rules (Top 5)

1. **Clean Architecture** — Domain layer has zero Spring dependencies. DTOs stay in `api/`, entities stay in `data/`. See [architecture rules](rules/architecture-api/).
2. **No direct commits to `develop`** — Always use a branch + PR. See [workflow](rules/general/workflow.md).
3. **Conventional commits** — `<type>(<scope>): <description>`. See [commit conventions](rules/general/commits.md).
4. **Tests required** — 80%+ coverage for services, all endpoints covered. Unit tests use MockK, no Spring context.
5. **Flyway migrations** — Never modify existing migration files. Add new `V<n>__<desc>.sql` files only.

---

## API Commands (from `api/` directory)

```bash
./gradlew compileKotlin     # Compile only
./gradlew test              # Run all tests
./gradlew ktlintFormat      # Auto-fix lint
./gradlew ktlintCheck       # Check lint
./gradlew bootRun           # Start API server
```

---

## Progress

See [rules/general/progress.md](rules/general/progress.md) for current MVP status.
