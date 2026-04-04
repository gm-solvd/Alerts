package com.privacyalert.presentation.util

/**
 * Resolves a localised string for the given [ErrorStringKey].
 *
 * Each platform provides its own `actual`:
 * - **Android** — can delegate to `context.getString(R.string.*)` when i18n is enabled.
 * - **iOS** — can delegate to `NSLocalizedString` when i18n is enabled.
 *
 * Until localisation is wired up, both platforms return English defaults.
 */
expect fun errorString(key: ErrorStringKey): String
