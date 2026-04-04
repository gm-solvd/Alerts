package com.privacyalert.presentation.util

/**
 * Keys for user-facing error messages.
 *
 * Each key maps to a platform-localised string via [errorString].
 * On Android the `actual` can resolve from `strings.xml`;
 * on iOS from `Localizable.strings`.
 */
enum class ErrorStringKey {
    SESSION_EXPIRED,
    NOT_FOUND,
    NETWORK_ERROR,
    SERVER_ERROR,
    CONFLICT,
    VALIDATION_ERROR,
    UNKNOWN_ERROR,
}
