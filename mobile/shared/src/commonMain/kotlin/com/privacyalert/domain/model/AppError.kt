package com.privacyalert.domain.model

sealed class AppError : Exception() {
    data class NetworkError(override val message: String = "Network error") : AppError()
    data class Unauthorized(override val message: String = "Unauthorized") : AppError()
    data class NotFound(override val message: String = "Not found") : AppError()
    data class Conflict(override val message: String = "Conflict") : AppError()
    data class ValidationError(
        override val message: String = "Validation error",
        val field: String? = null,
    ) : AppError()
    data class ServerError(override val message: String = "Server error") : AppError()
    data class UnknownError(override val message: String = "Unknown error") : AppError()
}
