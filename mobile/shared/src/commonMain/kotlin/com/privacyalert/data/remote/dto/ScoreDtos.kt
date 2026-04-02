package com.privacyalert.data.remote.dto

import com.privacyalert.domain.model.ScoreRecord
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ScoreResponseDto(
    val id: String,
    val score: Int,
    val recordedAt: Instant,
) {
    fun toDomain(): ScoreRecord = ScoreRecord(
        id = id,
        score = score,
        recordedAt = recordedAt,
    )
}
