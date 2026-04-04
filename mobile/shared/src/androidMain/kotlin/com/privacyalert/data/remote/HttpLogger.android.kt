package com.privacyalert.data.remote

import android.util.Log
import io.ktor.client.plugins.logging.Logger

actual val httpLogger: Logger = object : Logger {
    override fun log(message: String) {
        message.lines().forEach { line -> Log.d("Ktor", line) }
    }
}
