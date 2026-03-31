package com.privacyalert.api.dto

import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import java.time.Instant
import java.util.UUID

data class AdminUserResponse(
    val id: UUID,
    val email: String,
    val oauthProvider: String?,
    val alertCount: Long,
    val score: Int?,
    val createdAt: Instant,
)

data class AdminUserDetailResponse(
    val id: UUID,
    val email: String,
    val oauthProvider: String?,
    val alertCount: Long,
    val score: Int?,
    val recentAlerts: List<AlertResponse>,
    val createdAt: Instant,
)

data class AdminStatsResponse(
    val totalUsers: Long,
    val totalAlerts: Long,
    val alertsByCategory: Map<ThreatCategory, Long>,
    val alertsBySeverity: Map<Severity, Long>,
)
