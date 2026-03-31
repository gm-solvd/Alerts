package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class Mitigation(
    val id: UUID = UUID.randomUUID(),
    val alertId: UUID,
    val title: String,
    val description: String,
    val actionUrl: String? = null,
    val completed: Boolean = false,
    val completedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
)
