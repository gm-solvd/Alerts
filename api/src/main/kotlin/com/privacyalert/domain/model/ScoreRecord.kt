package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class ScoreRecord(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val score: Int,
    val recordedAt: Instant = Instant.now(),
)
