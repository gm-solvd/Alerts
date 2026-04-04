package com.privacyalert.presentation.util

import com.privacyalert.domain.model.AppError

/**
 * Maps a [Throwable] (typically an [AppError] subtype returned by [safeApiCall])
 * to a concise, user-facing message suitable for display in a UI error state.
 *
 * Delegates to [errorString] so each platform can resolve localised strings
 * from its native resource system (Android `strings.xml`, iOS `Localizable.strings`).
 *
 * Never exposes raw exception text to the user.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is AppError.Unauthorized -> errorString(ErrorStringKey.SESSION_EXPIRED)
    is AppError.NotFound -> errorString(ErrorStringKey.NOT_FOUND)
    is AppError.NetworkError -> errorString(ErrorStringKey.NETWORK_ERROR)
    is AppError.ServerError -> errorString(ErrorStringKey.SERVER_ERROR)
    is AppError.Conflict -> errorString(ErrorStringKey.CONFLICT)
    is AppError.ValidationError -> errorString(ErrorStringKey.VALIDATION_ERROR)
    else -> errorString(ErrorStringKey.UNKNOWN_ERROR)
}
