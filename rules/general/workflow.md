# Development Workflow

## Branching Strategy
```
main          → production-ready, protected
develop       → integration branch, all features merge here
feat/*        → feature branches off develop
fix/*         → bug fix branches off develop (or main for hotfixes)
refactor/*    → refactoring branches
```

### Protected Branches

- `main` and `develop` must **never** receive direct commits
- All work happens on feature/fix/refactor branches, merged via PR
- Exception: only when the user explicitly instructs a direct commit

### Behavior Changes Are Tasks Too

Any request to improve, adjust, or extend Claude's own behavior **must** go through the full task workflow — branch, implement, commit, push, PR — exactly like any code task. No exceptions.

This includes changes to:
- `rules/` — any `.md` rule file
- `.claude/skills/` — any skill (`SKILL.md`)
- `.claude/hooks/` — any hook script
- `.claude/rules/` — any on-demand rule
- `.claude/settings.json` or `.claude/settings.local.json`
- `CLAUDE.md`

Branch naming: use `chore/rules-<description>` for rule/config changes.

**Validation for config-only PRs**: if all changed files are under `.claude/`, `rules/`, or `CLAUDE.md` — **skip compile, lint, and test**. There is no Kotlin code to validate. Go straight from implementation to commit → push → PR → merge.

> Rationale: behavior changes are configuration-as-code. They affect every future task and must be reviewed, versioned, and reversible just like application code.

### Terminology

- **Feature** — top-level goal that may contain multiple tasks (e.g., "Async scan + structured findings")
- **Task** — a discrete workstream within a feature, one branch + PR per task:
  - `Web Backend` — Kotlin/Spring changes
  - `Web Frontend` — Vue admin changes
  - `Mobile UI` — Compose Multiplatform UI
  - `Mobile Business Logic` — KMP shared domain/use-cases

## Feature Development Flow
1. Branch off `develop`: `git checkout -b feat/<scope>-<description>`
2. Implement with tests
3. Validate locally: `./gradlew compileKotlin && ./gradlew ktlintCheck && ./gradlew test`
4. Group changes into atomic commits (stage files by name, never `git add .`)
5. Push to remote: `git push -u origin HEAD`
6. Create PR → `develop` via `gh pr create --base develop`
7. CI validates automatically (compile, lint, test via GitHub Actions)
8. If CI fails, Claude auto-fixes and pushes a commit to the PR branch
9. Address review comments
10. Squash merge into `develop` (manual — no auto-merge)
11. Delete feature branch

## Hotfix Flow
1. Branch off `main`: `git checkout -b fix/<description>`
2. Fix + test
3. Validate locally: `./gradlew compileKotlin && ./gradlew ktlintCheck && ./gradlew test`
4. Atomic commits, push, create PR → `main` via `gh pr create --base main`
5. CI validates automatically; Claude auto-fixes failures
6. After merge, back-merge into `develop`

## Local Dev Setup

### API
```bash
# Start Postgres
docker run -e POSTGRES_DB=privacyalert \
           -e POSTGRES_USER=dev \
           -e POSTGRES_PASSWORD=dev \
           -p 5432:5432 postgres:16

# Run API
cd api && ./gradlew bootRun
```

### Mobile
```bash
# Android
cd mobile && ./gradlew :androidApp:installDebug

# iOS — open in Xcode
open mobile/iosApp/iosApp.xcodeproj
```

## Environment Variables
Copy `.env.example` to `.env` and fill in:
```
DB_URL=jdbc:postgresql://localhost:5432/privacyalert
DB_USER=dev
DB_PASSWORD=dev
JWT_SECRET=<generate a secure random string>
HIBP_API_KEY=<obtain from haveibeenpwned.com>
GOOGLE_CLIENT_ID=<from Google Cloud Console>
GOOGLE_CLIENT_SECRET=<from Google Cloud Console>
```

## Before Opening a PR
- [ ] Tests pass locally: `cd api && ./gradlew test`
- [ ] No lint warnings: `cd api && ./gradlew ktlintCheck`
- [ ] API contract unchanged (or updated DTO docs)
- [ ] Migration script added if DB schema changed
