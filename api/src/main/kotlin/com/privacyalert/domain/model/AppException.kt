package com.privacyalert.domain.model

sealed class AppException(message: String) : RuntimeException(message) {

    class ResourceNotFoundException(
        val resource: String,
        val id: Any,
    ) : AppException("$resource with id $id not found")

    class ValidationException(
        override val message: String,
    ) : AppException(message)

    class ConflictException(
        override val message: String,
    ) : AppException(message)

    class ExternalServiceException(
        val service: String,
        val reason: String,
    ) : AppException("External service '$service' failed: $reason")

    class UnauthorizedException(
        override val message: String,
    ) : AppException(message)
}
