package com.privacyalert.presentation.util

/**
 * Multiplatform logger. Logs to Logcat on Android and NSLog/print on iOS.
 * Use for error diagnostics — every use-case failure should be logged
 * before being mapped to a user-facing message.
 */
expect object AppLogger {
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String)
}
