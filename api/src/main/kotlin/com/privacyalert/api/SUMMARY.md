# API Layer

Spring Web MVC layer. Receives HTTP requests, delegates to domain services, returns JSON responses. Controllers are intentionally thin — no business logic lives here.

## Sub-directories

| Directory | Purpose |
|-----------|---------|
| [`controller/`](controller/SUMMARY.md) | `@RestController` classes for alerts, auth, scan, score, mitigations, audit, admin + global exception handler |
| [`dto/`](dto/SUMMARY.md) | Request/response objects — never expose domain models directly to clients |

## Request Flow

```
HTTP Request
  → JwtAuthFilter (validates Bearer token, sets SecurityContext)
  → @RestController (parse DTO, extract userId from SecurityContext)
  → Domain Service (business logic)
  → Domain Model → DTO mapping
  → HTTP Response
```

## Error Handling

`GlobalExceptionHandler` catches domain exceptions and maps them to HTTP status codes:
- `ResourceNotFoundException` → 404
- `ConflictException` → 409
- `UnauthorizedException` → 401
- `ForbiddenException` → 403
- `BadRequestException` → 400
