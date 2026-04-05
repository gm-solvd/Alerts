package com.privacyalert.api.dto

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import java.time.Instant
import java.util.UUID

data class AlertResponse(
    val id: UUID,
    val category: ThreatCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val resolved: Boolean,
    val resolvedAt: Instant?,
    val createdAt: Instant,
    val tags: List<String> = emptyList(),
)

fun Alert.toResponse(): AlertResponse =
    AlertResponse(
        id = id,
        category = category,
        severity = severity,
        title = title,
        description = description,
        resolved = resolved,
        resolvedAt = resolvedAt,
        createdAt = createdAt,
        tags = tags,
    )
