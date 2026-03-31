package com.privacyalert.domain.service

import java.util.UUID

interface JwtProvider {
    fun generateAccessToken(
        userId: UUID,
        email: String,
    ): String

    fun generateRefreshToken(): String

    fun validateAndExtractUserId(token: String): UUID?
}
