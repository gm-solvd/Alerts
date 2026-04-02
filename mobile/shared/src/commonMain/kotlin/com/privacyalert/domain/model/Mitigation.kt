package com.privacyalert.domain.model

import kotlinx.datetime.Instant

data class Mitigation(
    val id: String,
    val alertId: String,
    val title: String,
    val description: String,
    val actionUrl: String?,
    val completed: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
)
