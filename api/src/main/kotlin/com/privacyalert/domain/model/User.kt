package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val passwordHash: String? = null,
    val oauthProvider: String? = null,
    val oauthSubject: String? = null,
    val createdAt: Instant = Instant.now(),
)
