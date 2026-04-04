package com.privacyalert.data.remote

import com.privacyalert.domain.model.AppError
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

/**
 * Throws the appropriate [AppError] subtype for any non-2xx response status
 * before [io.ktor.client.call.body] is called.
 *
 * This prevents Ktor's ContentNegotiation plugin from attempting to deserialise
 * an error response body (which often has no Content-Type header) and throwing
 * a cryptic NoTransformationFoundException.
 */
internal fun HttpResponse.throwIfError() {
    statusToAppError(status)?.let { throw it }
}

@Suppress("MagicNumber")
internal fun statusToAppError(status: HttpStatusCode): AppError? = when {
    status == HttpStatusCode.Unauthorized -> AppError.Unauthorized()
    status == HttpStatusCode.NotFound -> AppError.NotFound()
    status == HttpStatusCode.Conflict -> AppError.Conflict()
    status.value in 400..499 -> AppError.ValidationError("Request error: ${status.description}")
    status.value in 500..599 -> AppError.ServerError("Server error: ${status.description}")
    else -> null
}
