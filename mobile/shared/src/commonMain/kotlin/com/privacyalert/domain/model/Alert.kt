package com.privacyalert.domain.model

import kotlinx.datetime.Instant

data class Alert(
    val id: String,
    val category: ThreatCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val resolved: Boolean,
    val resolvedAt: Instant?,
    val createdAt: Instant,
    val tags: List<String> = emptyList(),
)
