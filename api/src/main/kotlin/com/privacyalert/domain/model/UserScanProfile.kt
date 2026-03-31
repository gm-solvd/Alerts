package com.privacyalert.domain.model

import java.time.LocalDate

data class UserScanProfile(
    val email: String,
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: LocalDate? = null,
)
