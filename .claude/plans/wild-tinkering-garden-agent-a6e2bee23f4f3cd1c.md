# Implementation Plan: Admin User CRUD + Scan Trigger + Scan Progress

## Context

The admin web interface exists (Vue.js SPA + API admin endpoints) but only supports viewing/deleting users. The goal is to:
1. Let the admin **create test users** with scan profile data (email, name, phone, etc.)
2. **Trigger scans** from the admin UI via a "Run Scan" button
3. **View raw scan results** (breach details, not just alert summaries)
4. **Monitor scan progress** — see which scanners ran, their status, and result counts

All scanning uses existing mock/local scanner implementations (no real external APIs).

---

## Phase 1: API — Admin Create User + Profile Fields

**Goal:** `POST /api/v1/admin/users` creates a user with scan profile data stored on the user record.

### DB Migration
- `V12__add_profile_columns_to_users.sql` — adds `full_name VARCHAR(255)`, `phone_number VARCHAR(50)`, `home_address VARCHAR(500)`, `date_of_birth DATE` to `users` table

### Files to modify
- `api/.../domain/model/User.kt` — add `fullName`, `phoneNumber`, `homeAddress`, `dateOfBirth` optional fields
- `api/.../data/entity/UserEntity.kt` — add columns + update `toDomain()`/`toEntity()` mappers
- `api/.../api/dto/AdminDtos.kt` — add `CreateUserRequest(email, fullName, phoneNumber?, homeAddress?, dateOfBirth?)`, update `AdminUserResponse` and `AdminUserDetailResponse` to include profile fields
- `api/.../domain/service/AdminService.kt` — add `createUser()` method
- `api/.../api/controller/AdminController.kt` — add `POST /users` endpoint

### Commit
`feat(api): add admin create user endpoint with scan profile fields`

---

## Phase 2: API — Admin Trigger Scan + ScanResult Tracking

**Goal:** `POST /api/v1/admin/users/{id}/scan` triggers a full scan, saves `ScanResult` per scanner, returns structured results.

### Files to modify
- `api/.../domain/service/ScanService.kt` — inject `ScanResultRepository`, save a `ScanResult` after each scanner runs (scanType=breach/identity/pii/social, findings=raw JSON)
- `api/.../domain/service/AdminService.kt` — add `triggerScan(userId)` that builds `UserScanProfile` from user fields, calls `scanService.fullScan()`, returns per-scanner results
- `api/.../api/controller/AdminController.kt` — add `POST /users/{id}/scan`
- `api/.../api/dto/AdminDtos.kt` — add `ScanExecutionResponse` with per-scanner breakdown (scannerName, findingsCount, alerts list)

### Commit
`feat(api): add admin scan trigger endpoint with ScanResult tracking`

---

## Phase 3: API — Scan History Endpoint

**Goal:** `GET /api/v1/admin/users/{id}/scans` returns all scan executions for a user.

### Files to modify
- `api/.../domain/repository/ScanResultRepository.kt` — add `findAllByUserId(userId, pageable): Page<ScanResult>`
- `api/.../data/repository/ScanResultJpaRepository.kt` — add `findAllByUserId(UUID, Pageable)`
- `api/.../data/repository/ScanResultRepositoryAdapter.kt` — implement new method
- `api/.../domain/service/AdminService.kt` — add `getScanHistory(userId, pageable)`
- `api/.../api/controller/AdminController.kt` — add `GET /users/{id}/scans`
- `api/.../api/dto/AdminDtos.kt` — add `ScanResultResponse(id, scanType, findingsCount, findings, createdAt)`

### Commit
`feat(api): add scan history endpoint for admin progress monitoring`

---

## Phase 4: Vue.js — Create User Form

### Files to create
- `admin/src/views/CreateUser.vue` — form with: email (required), fullName (required), phoneNumber, homeAddress, dateOfBirth. On submit -> POST /admin/users -> redirect to user detail.

### Files to modify
- `admin/src/api.js` — add `createUser(data)`, `triggerScan(id)`, `getScanHistory(id)` methods
- `admin/src/router.js` — add `/users/new` route
- `admin/src/views/UserList.vue` — add "Create User" button

### Commit
`feat(admin): add create user form page`

---

## Phase 5: Vue.js — Scan Trigger + Raw Results Display

### Files to modify
- `admin/src/views/UserDetail.vue` — Add profile fields display, "Run Scan" button. On click: call POST /admin/users/{id}/scan, show spinner, then render raw per-scanner results (breach names/domains/dates, PII source URLs, identity platforms, social profiles). Expand alert descriptions.

### Commit
`feat(admin): add scan trigger button and raw breach detail display`

---

## Phase 6: Vue.js — Scan History Page

### Files to create
- `admin/src/views/ScanHistory.vue` — Table of past scans: timestamp, scan type, findings count, expandable raw JSON. Grouped by execution time.

### Files to modify
- `admin/src/router.js` — add `/users/:id/scans` route
- `admin/src/views/UserDetail.vue` — add "Scan History" link

### Commit
`feat(admin): add scan history page with per-scanner progress`

---

## Verification

1. `./gradlew compileKotlin` — passes
2. `./gradlew ktlintFormat && ./gradlew ktlintCheck` — passes
3. `./gradlew test` — existing tests pass
4. `npm run build` (in admin/) — succeeds
5. Manual E2E with `./start.sh`: create user, trigger scan, view raw results, check scan history
