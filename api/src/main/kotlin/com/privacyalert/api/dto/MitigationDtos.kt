package com.privacyalert.api.dto

import com.privacyalert.domain.model.Mitigation
import java.time.Instant
import java.util.UUID

data class MitigationResponse(
    val id: UUID,
    val alertId: UUID,
    val title: String,
    val description: String,
    val actionUrl: String?,
    val completed: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
)

fun Mitigation.toResponse(): MitigationResponse =
    MitigationResponse(
        id = id,
        alertId = alertId,
        title = title,
        description = description,
        actionUrl = actionUrl,
        completed = completed,
        completedAt = completedAt,
        createdAt = createdAt,
    )
