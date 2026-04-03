package com.privacyalert.domain.util

import com.privacyalert.domain.model.AppError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Suppress("CyclomaticComplexMethod")
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is ClientRequestException -> when (response.status) {
        HttpStatusCode.Unauthorized -> AppError.Unauthorized()
        HttpStatusCode.NotFound -> AppError.NotFound()
        HttpStatusCode.Conflict -> AppError.Conflict()
        else -> AppError.ValidationError(
            message = message ?: "Request error (${response.status.value})",
        )
    }
    is ServerResponseException -> AppError.ServerError(
        message = message ?: "Server error (${response.status.value})",
    )
    else -> if (this::class.simpleName?.contains("IOException") == true ||
        this::class.simpleName?.contains("ConnectException") == true
    ) {
        AppError.NetworkError()
    } else {
        AppError.UnknownError(message = message ?: "Unknown error")
    }
}

@Suppress("TooGenericExceptionCaught")
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: AppError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e.toAppError())
    }

@Suppress("TooGenericExceptionCaught")
fun <T> safeFlow(block: suspend () -> T): Flow<Result<T>> = flow {
    emit(
        try {
            Result.success(block())
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e.toAppError())
        },
    )
}
