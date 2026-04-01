# Controllers

Thin `@RestController` classes. Each controller parses HTTP input, calls the relevant domain service, and maps the result to a DTO response. No business logic here.

## Files

| File | Base Path | Endpoints |
|------|-----------|-----------|
| `AlertController.kt` | `/api/v1/alerts` | `GET /` (list, filterable by category/severity + pagination), `GET /{id}`, `PATCH /{id}/resolve`, `DELETE /{id}` |
| `AuthController.kt` | `/api/v1/auth` | `POST /register`, `POST /login`, `POST /oauth2/callback`, `POST /refresh` |
| `ScanController.kt` | `/api/v1/scan` | `POST /breach`, `POST /identity`, `POST /pii`, `POST /social`, `POST /full` |
| `ScoreController.kt` | `/api/v1/score` | `GET /` (current score), `GET /history` (paginated) |
| `MitigationController.kt` | `/api/v1/mitigations` | `GET /` (by userId or alertId), `PATCH /{id}/complete` |
| `PermissionAuditController.kt` | `/api/v1/audit` | `POST /permissions` (submit Android permission list for analysis) |
| `AdminController.kt` | `/api/v1/admin` | `POST /users`, `GET /users`, `GET /users/{id}`, `DELETE /users/{id}`, `GET /users/{id}/alerts`, `POST /users/{id}/scan`, `GET /users/{id}/scans`, `GET /stats` — all require `ROLE_ADMIN` |
| `GlobalExceptionHandler.kt` | — | `@ControllerAdvice` mapping domain exceptions to HTTP status codes |
| `AuthenticatedUser.kt` | — | Helper extracting authenticated user UUID from `SecurityContext` |

## Auth Requirements

- Public (no token): `/api/v1/auth/**`
- Authenticated (valid JWT): all other user endpoints
- Admin only (`ROLE_ADMIN`): `/api/v1/admin/**`
