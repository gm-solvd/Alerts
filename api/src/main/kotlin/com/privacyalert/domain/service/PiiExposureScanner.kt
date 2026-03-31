package com.privacyalert.domain.service

import com.privacyalert.domain.model.UserScanProfile

data class PiiExposureResult(
    val source: String,
    val sourceUrl: String,
    val exposedFields: List<String>,
    val snippet: String? = null,
)

interface PiiExposureScanner {
    fun scan(profile: UserScanProfile): List<PiiExposureResult>
}
