package com.privacyalert.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BreachScanRequestDto(
    val email: String,
)

@Serializable
data class ScanProfileRequestDto(
    val email: String,
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: String? = null,
    val username: String? = null,
)
