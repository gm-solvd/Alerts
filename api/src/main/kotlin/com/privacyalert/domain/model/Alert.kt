package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class Alert(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val category: ThreatCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val resolved: Boolean = false,
    val resolvedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val tags: List<String> = emptyList(),
)
