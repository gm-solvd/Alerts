package com.privacyalert.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val passwordHash: String? = null,
    val oauthProvider: String? = null,
    val oauthSubject: String? = null,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: LocalDate? = null,
    val createdAt: Instant = Instant.now(),
)
