package com.privacyalert.data.remote.dto

import com.privacyalert.domain.model.AuthTokens
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class AuthTokensResponseDto(
    val accessToken: String,
    val refreshToken: String,
) {
    fun toDomain(): AuthTokens = AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}
