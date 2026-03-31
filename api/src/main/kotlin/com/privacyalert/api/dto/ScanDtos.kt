package com.privacyalert.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

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

data class ScanProfileRequest(
    @field:NotBlank
    @field:Email
    val email: String,
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: LocalDate? = null,
    val username: String? = null,
)
