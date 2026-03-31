package com.privacyalert.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class BreachScanRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)

data class IdentityScanRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)
