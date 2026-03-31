# Error Handling — API

## Strategy
Use a global exception handler (`@ControllerAdvice`) to translate domain exceptions into consistent HTTP responses. Controllers do **not** catch exceptions.

---

## Exception Hierarchy

```kotlin
// Base
sealed class AppException(message: String) : RuntimeException(message)

// Domain exceptions
class ResourceNotFoundException(resource: String, id: Any) :
    AppException("$resource not found: $id")

class ValidationException(message: String) :
    AppException(message)

class ConflictException(message: String) :
    AppException(message)

class ExternalServiceException(service: String, cause: String) :
    AppException("$service error: $cause")

class UnauthorizedException(message: String = "Unauthorized") :
    AppException(message)
```

---

## Error Response DTO

```kotlin
data class ErrorResponse(
    val code: String,        // machine-readable, e.g. "RESOURCE_NOT_FOUND"
    val message: String,     // human-readable
    val timestamp: Instant = Instant.now()
)
```

---

## Global Exception Handler

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException) =
        ResponseEntity.status(404).body(ErrorResponse("RESOURCE_NOT_FOUND", ex.message!!))

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException) =
        ResponseEntity.status(400).body(ErrorResponse("VALIDATION_ERROR", ex.message!!))

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException) =
        ResponseEntity.status(401).body(ErrorResponse("UNAUTHORIZED", ex.message!!))

    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternal(ex: ExternalServiceException) =
        ResponseEntity.status(502).body(ErrorResponse("EXTERNAL_SERVICE_ERROR", ex.message!!))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception) =
        ResponseEntity.status(500).body(ErrorResponse("INTERNAL_ERROR", "Unexpected error"))
}
```

---

## Rules
- Services **throw** domain exceptions; they never return nulls for missing resources
- Controllers never wrap calls in try/catch — let the handler do it
- Log the full stack trace at ERROR level in the handler for 5xx; use WARN for 4xx
- External API failures (HIBP, OAuth) always throw `ExternalServiceException`
