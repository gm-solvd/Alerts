package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val revoked: Boolean = false,
    val createdAt: Instant = Instant.now(),
)
