package com.privacyalert.domain.service

data class SocialFootprintResult(
    val platform: String,
    val profileUrl: String,
    val username: String,
    val publicInfoFound: List<String>,
)

interface SocialFootprintScanner {
    fun scan(email: String, fullName: String?, username: String?): List<SocialFootprintResult>
}
