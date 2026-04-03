package com.privacyalert.data.remote.dto

import com.privacyalert.domain.model.Mitigation
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MitigationResponseDto(
    val id: String,
    val alertId: String,
    val title: String,
    val description: String,
    val actionUrl: String? = null,
    val completed: Boolean,
    val completedAt: Instant? = null,
    val createdAt: Instant,
) {
    fun toDomain(): Mitigation = Mitigation(
        id = id,
        alertId = alertId,
        title = title,
        description = description,
        actionUrl = actionUrl,
        completed = completed,
        completedAt = completedAt,
        createdAt = createdAt,
    )
}
