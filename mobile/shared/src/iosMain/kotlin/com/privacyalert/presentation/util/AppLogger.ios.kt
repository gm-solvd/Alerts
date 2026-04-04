package com.privacyalert.presentation.util

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        NSLog("E/$tag: $message${throwable?.let { " — ${it.stackTraceToString()}" } ?: ""}")
    }

    actual fun w(tag: String, message: String, throwable: Throwable?) {
        NSLog("W/$tag: $message${throwable?.let { " — ${it.message}" } ?: ""}")
    }

    actual fun d(tag: String, message: String) {
        NSLog("D/$tag: $message")
    }
}
