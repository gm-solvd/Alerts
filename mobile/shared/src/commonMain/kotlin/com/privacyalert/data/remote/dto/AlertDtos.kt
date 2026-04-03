package com.privacyalert.data.remote.dto

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AlertResponseDto(
    val id: String,
    val category: ThreatCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val resolved: Boolean,
    val resolvedAt: Instant? = null,
    val createdAt: Instant,
) {
    fun toDomain(): Alert = Alert(
        id = id,
        category = category,
        severity = severity,
        title = title,
        description = description,
        resolved = resolved,
        resolvedAt = resolvedAt,
        createdAt = createdAt,
    )
}
