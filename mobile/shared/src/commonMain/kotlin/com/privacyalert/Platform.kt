package com.privacyalert

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
