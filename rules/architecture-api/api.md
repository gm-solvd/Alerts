# API Conventions

## Base URL
```
/api/v1/
```

## Endpoint Patterns

| Action | Method | Path |
|---|---|---|
| List resources | GET | `/api/v1/alerts` |
| Get single | GET | `/api/v1/alerts/{id}` |
| Create | POST | `/api/v1/alerts` |
| Partial update / action | PATCH | `/api/v1/alerts/{id}/resolve` |
| Delete | DELETE | `/api/v1/alerts/{id}` |

- Use **nouns**, not verbs in paths (`/alerts`, not `/getAlerts`)
- Use **PATCH** for partial updates and state transitions (resolve, dismiss)
- Use **POST** for actions that create a new resource or trigger a side effect (`/scan/breach`)

---

## Endpoints

### Auth
```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/oauth2/callback
POST /api/v1/auth/refresh
```

### Alerts
```
GET    /api/v1/alerts              ?category=&severity=&page=&size=
GET    /api/v1/alerts/{id}
PATCH  /api/v1/alerts/{id}/resolve
DELETE /api/v1/alerts/{id}
```

### Scan
```
POST /api/v1/scan/breach           body: { email }
POST /api/v1/scan/identity         body: { email } → social media footprint search
GET  /api/v1/scan/history
```

### Permission Audit
```
POST /api/v1/audit/permissions     body: { permissions: [...] } ← mobile sends device data
GET  /api/v1/audit/permissions/history
```

### Score
```
GET /api/v1/score
GET /api/v1/score/history
```

### Mitigations
```
GET /api/v1/mitigations
GET /api/v1/mitigations/{alertId}
```

---

## DTO Conventions

- Separate `Request` and `Response` DTOs — never reuse the same class for both
- Use `data class` with explicit field names (no `Map<String, Any>`)
- Validation annotations go on Request DTOs only
- Use `Instant` for timestamps (ISO-8601 in JSON)
- Naming: `AlertResponse`, `CreateAlertRequest`, `ResolveAlertResponse`

```kotlin
data class AlertResponse(
    val id: UUID,
    val category: ThreatCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val resolved: Boolean,
    val createdAt: Instant
)
```

---

## Pagination
List endpoints return a page wrapper:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

Use Spring's `Pageable` + `Page<T>` internally.

---

## HTTP Status Codes

| Situation | Code |
|---|---|
| Success (with body) | 200 |
| Created | 201 |
| No content | 204 |
| Bad request / validation | 400 |
| Unauthorized | 401 |
| Forbidden | 403 |
| Not found | 404 |
| External service error | 502 |
| Unexpected server error | 500 |
