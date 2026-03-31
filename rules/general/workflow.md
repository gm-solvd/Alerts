# Development Workflow

## Branching Strategy
```
main          → production-ready, protected
develop       → integration branch, all features merge here
feat/*        → feature branches off develop
fix/*         → bug fix branches off develop (or main for hotfixes)
refactor/*    → refactoring branches
```

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
