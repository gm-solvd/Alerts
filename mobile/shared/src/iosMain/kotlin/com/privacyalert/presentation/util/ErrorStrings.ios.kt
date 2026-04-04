package com.privacyalert.presentation.util

/**
 * iOS implementation of [errorString].
 *
 * Returns English defaults. To enable i18n, replace with a resolver
 * backed by `NSLocalizedString` and provide translations in
 * `Localizable.strings` for each supported locale.
 */
actual fun errorString(key: ErrorStringKey): String = when (key) {
    ErrorStringKey.SESSION_EXPIRED ->
        "Your session has expired. Please log in again."
    ErrorStringKey.NOT_FOUND ->
        "The requested data could not be found."
    ErrorStringKey.NETWORK_ERROR ->
        "No internet connection. Please check your network and try again."
    ErrorStringKey.SERVER_ERROR ->
        "Something went wrong on our end. Please try again later."
    ErrorStringKey.CONFLICT ->
        "A conflict occurred. Please refresh and try again."
    ErrorStringKey.VALIDATION_ERROR ->
        "Invalid request. Please try again."
    ErrorStringKey.UNKNOWN_ERROR ->
        "Something went wrong. Please try again."
}
