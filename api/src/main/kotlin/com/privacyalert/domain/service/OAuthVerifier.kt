package com.privacyalert.domain.service

data class OAuthUserInfo(
    val provider: String,
    val subject: String,
    val email: String,
)

interface OAuthVerifier {
    fun verify(
        provider: String,
        idToken: String,
    ): OAuthUserInfo
}
