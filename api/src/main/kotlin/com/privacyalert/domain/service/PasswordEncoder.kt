package com.privacyalert.domain.service

interface PasswordEncoder {
    fun hash(rawPassword: String): String

    fun matches(
        rawPassword: String,
        encodedPassword: String,
    ): Boolean
}
