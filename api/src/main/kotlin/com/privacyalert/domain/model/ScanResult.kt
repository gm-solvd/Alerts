package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class ScanResult(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val scanType: String,
    val scanInput: String,
    val findings: String = "[]",
    val createdAt: Instant = Instant.now(),
)
