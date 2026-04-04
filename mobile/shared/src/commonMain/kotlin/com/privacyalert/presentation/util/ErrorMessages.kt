package com.privacyalert.presentation.util

import com.privacyalert.domain.model.AppError

/**
 * Maps a [Throwable] (typically an [AppError] subtype returned by [safeApiCall])
 * to a concise, user-facing message suitable for display in a UI error state.
 *
 * Never exposes raw exception text to the user.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is AppError.Unauthorized -> "Your session has expired. Please log in again."
    is AppError.NotFound -> "The requested data could not be found."
    is AppError.NetworkError -> "No internet connection. Please check your network and try again."
    is AppError.ServerError -> "Something went wrong on our end. Please try again later."
    is AppError.Conflict -> "A conflict occurred. Please refresh and try again."
    is AppError.ValidationError -> "Invalid request. Please try again."
    else -> "Something went wrong. Please try again."
}
