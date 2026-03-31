package com.privacyalert.api.dto

import com.privacyalert.domain.model.ScoreRecord
import java.time.Instant
import java.util.UUID

data class ScoreResponse(
    val id: UUID,
    val score: Int,
    val recordedAt: Instant,
)

fun ScoreRecord.toResponse(): ScoreResponse =
    ScoreResponse(
        id = id,
        score = score,
        recordedAt = recordedAt,
    )
