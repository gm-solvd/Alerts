---
paths:
  - "api/**/exception/**"
  - "api/**/*Exception*"
  - "api/**/GlobalExceptionHandler*"
---

# Error Handling — API

## Exception Hierarchy

`AppException` sealed class with subtypes:
- `ResourceNotFoundException` → 404
- `ValidationException` → 400
- `ConflictException` → 409
- `ExternalServiceException` → 502
- `UnauthorizedException` → 401

## Rules

- Services **throw** domain exceptions — never return nulls for not-found cases
- Controllers **never catch** exceptions — let `@ControllerAdvice` handle them
- Log full stack traces for 5xx errors, structured messages for 4xx
- `ErrorResponse` DTO: `{ code: String, message: String, timestamp: Instant }`

Full reference: `rules/architecture-api/error-handling.md`
