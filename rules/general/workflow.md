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
3. Open PR → `develop`
4. Address review comments
5. Squash merge into `develop`
6. Delete feature branch

## Hotfix Flow
1. Branch off `main`: `git checkout -b fix/<description>`
2. Fix + test
3. PR → `main` and back-merge into `develop`

## Local Dev Setup

### API
```bash
# Start Postgres
docker run -e POSTGRES_DB=privacyalert \
           -e POSTGRES_USER=dev \
           -e POSTGRES_PASSWORD=dev \
           -p 5432:5432 postgres:16

# Run API
./gradlew :api:bootRun
```

### Mobile
```bash
# Android
./gradlew :mobile:androidApp:installDebug

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
- [ ] Tests pass locally: `./gradlew test`
- [ ] No lint warnings: `./gradlew ktlintCheck`
- [ ] API contract unchanged (or updated DTO docs)
- [ ] Migration script added if DB schema changed
