package com.privacyalert.domain.service

data class IdentityExposureResult(
    val source: String,
    val profileUrl: String,
    val displayName: String? = null,
    val exposedFields: List<String>,
)

interface IdentityExposureScanner {
    fun scan(
        email: String,
        fullName: String?,
    ): List<IdentityExposureResult>
}
