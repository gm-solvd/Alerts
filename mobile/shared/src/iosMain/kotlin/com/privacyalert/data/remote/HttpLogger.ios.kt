package com.privacyalert.data.remote

import io.ktor.client.plugins.logging.Logger

private const val MAX_LOG_LENGTH = 4000

actual val httpLogger: Logger = object : Logger {
    override fun log(message: String) {
        message.lines().forEach { line ->
            if (line.length <= MAX_LOG_LENGTH) {
                println("Ktor: $line")
            } else {
                line.chunked(MAX_LOG_LENGTH).forEach { chunk ->
                    println("Ktor: $chunk")
                }
            }
        }
    }
}
