package com.privacyalert.api.dto

import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class CreateUserRequest(
    val email: String,
    val fullName: String,
    val phoneNumber: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: LocalDate? = null,
)

data class AdminUserResponse(
    val id: UUID,
    val email: String,
    val fullName: String?,
    val oauthProvider: String?,
    val alertCount: Long,
    val score: Int?,
    val createdAt: Instant,
)

data class AdminUserDetailResponse(
    val id: UUID,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val homeAddress: String?,
    val dateOfBirth: LocalDate?,
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

data class ScanJobResponse(
    val jobId: UUID,
    val status: String,
    val progress: String?,
    val totalAlerts: Int?,
    val errorMessage: String?,
    val startedAt: Instant?,
    val completedAt: Instant?,
)

data class StructuredFindingResponse(
    val type: String,
    val name: String,
    val sourceUrl: String?,
    val date: String?,
    val dataClasses: List<String>,
    val severity: String?,
    val recordCount: Long?,
    val exposedFields: List<String>,
)

data class ScanResultResponse(
    val id: UUID,
    val scanType: String,
    val findings: String,
    val details: List<StructuredFindingResponse>,
    val createdAt: Instant,
)
