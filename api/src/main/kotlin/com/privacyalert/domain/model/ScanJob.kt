package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class ScanJob(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val status: ScanJobStatus = ScanJobStatus.PENDING,
    val progress: String? = null,
    val totalAlerts: Int? = null,
    val errorMessage: String? = null,
    val startedAt: Instant? = null,
    val completedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
)

enum class ScanJobStatus { PENDING, RUNNING, COMPLETED, FAILED }
