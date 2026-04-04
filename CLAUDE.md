# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Privacy Alert System — a mobile-first app that monitors digital exposure, scores privacy health, and sends categorized threat alerts with actionable mitigations. Monorepo with `api/` (Kotlin + Spring Boot) and `mobile/` (KMP + Compose Multiplatform).

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

All mobile commands run from the `mobile/` directory. Android SDK is required (`export ANDROID_HOME=~/Library/Android/sdk`).

```bash
# Compile shared KMP module
./gradlew :shared:compileDebugKotlinAndroid

# Detekt static analysis
./gradlew :shared:detektMetadataCommonMain

# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Run all shared tests
./gradlew :shared:allTests
```

## Architecture

Clean Architecture with strict layer isolation. Both API and mobile follow the dependency rule: each layer depends only on layers below it. Domain has **zero** framework dependencies.

Architecture rules auto-load from `.claude/rules/` when editing matching files:
- `api/**/*.kt` → API architecture, DI, REST conventions
- `api/**/auth/**` → Authentication (JWT, OAuth2)
- `api/**/data/**` → Database (PostgreSQL, Flyway, entities)
- `api/**/score/**` → Scoring algorithm (CVSS-inspired)
- `api/**/exception/**` → Error handling (@ControllerAdvice)
- `api/**/*Test.kt` → API testing (MockK, Testcontainers)
- `mobile/**/*.kt` → KMP architecture, Koin DI
- `mobile/**/presentation/**` → Compose components, styling, navigation
- `mobile/**/*Test.kt` → Mobile testing (Turbine, MockEngine)

Full reference: `rules/architecture-api/` and `rules/architecture-mobile/`

## Terminology

- **Feature** — the top-level goal (e.g., "Async scan + structured findings"). Maps to a plan.
- **Task** — a discrete workstream within a feature (e.g., "Web Backend: async scan execution", "Web Frontend: polling UI").

## Task Split

Every feature must be divided into tasks by workstream before implementation:

- **Web Backend** — Kotlin/Spring Boot changes (domain, data, api, integration, config layers)
- **Web Frontend** — Vue admin dashboard changes
- **Mobile UI** — Compose Multiplatform UI layer changes
- **Mobile Business Logic** — KMP shared domain/use-case changes

Each task becomes its own branch and PR. Related tasks use a parent/child branch pattern: children branch off and merge into the parent, then the parent merges into develop when all tasks are complete.

## Implementation Workflow

> ⚠️ **Never commit directly to `develop` or `main`.** Always work on a feature/fix branch. The only exception is when the user explicitly instructs a direct commit.

**Every feature must follow these steps with STOP checkpoints:**

1. **Task Intake** — Summarize understanding. **STOP** for confirmation.
2. **Branch Creation** — `git fetch origin && git checkout develop && git pull origin develop && git checkout -b <type>/<scope>-<description>`. **STOP** for approval.
3. **Planning** — Create implementation plan with tasks split by workstream. **STOP** for approval.
4. **Implementation** — Implement step by step. **NO COMMITS** yet.
5. **Validation** — Run `compileKotlin`, `ktlintFormat`, `ktlintCheck`, `detekt` (mobile), `test`. Fix unused code. **STOP** to report results. *(Skip entirely for config-only changes — `.claude/`, `rules/`, `CLAUDE.md`.)*
6. **Human Review** — Show `git diff`, wait for user review.
7. **Atomic Commits** — Group changes logically. Stage files by name (never `git add .`).
8. **Push & Pull Request** — Push branch, draft PR, **STOP** for approval, then `gh pr create --base develop`. Return PR URL.
8.5. **Code Review & Merge** — `/review` posts findings as PR comment tagging @gm-solvd. 0 problems → auto-merge. Problems found → auto-fix, validate, re-review (max 3 cycles). macOS notifications at each phase.
9. **Update Docs** — Run `/docs` to update `SUMMARY.md` files for any changed directories and refresh `README.md`.
10. **Update Progress** — Update `rules/general/progress.md`. **DO NOT STOP** — immediately start the next pending phase. Only stop when all phases are DONE or user explicitly asks.
11. **All Phases Complete** — When every phase is DONE, work through `rules/general/later-improvements.md` items (branch, implement, validate, PR, review each). Only after all phases AND all later-improvement items are finished, show a prominent `display dialog` notification.

## Branching & Commits

```
main     → production-ready, protected — no direct commits
develop  → integration branch — no direct commits
feat/*   → off develop     fix/*    → off develop (or main for hotfixes)
```

> All commits must go to a feature/fix branch. Merging to `develop` or `main` happens only via PR.

Branch naming: `<type>/<scope>-<short-description>` (e.g., `feat/scan-pii-exposure`).

Commit format: `<type>(<scope>): <description>` — imperative mood, max 72 chars, no period.
Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`, `perf`.
Scopes: `mobile`, `android`, `ios`, `api`, `db`, `auth`, `alerts`, `score`, `scan`.

## Rules & Skills Reference

Detailed rules in `rules/` — auto-loading condensed versions in `.claude/rules/`:
- `rules/general/` — workflow, commits, PRs, progress tracking
- `rules/architecture-api/` — clean architecture, API conventions, database, auth, scoring, testing, error handling, DI
- `rules/architecture-mobile/` — KMP architecture, components, navigation, styling, testing

Custom automation skills in `.claude/skills/` — `workflow`, `branch`, `commit`, `pr`, `review`, `validate`, `test`, `lint`, `push`, `implement`, `progress`, `docs`.

Safety hooks in `.claude/hooks/` enforce: no commits on protected branches, no `git add .`, no force push, no push to main/develop, no committing secrets.

> ⚠️ **Any request to improve Claude's behavior** (rules, skills, hooks, settings, CLAUDE.md) is a task — full workflow applies: `chore/rules-<description>` branch → implement → commit → push → PR. No exceptions.

## Documentation Summaries

Every directory has a `SUMMARY.md` describing its files and business logic. Run `/docs` after any code change to keep them in sync. The `README.md` links to all summaries and rule documents.
