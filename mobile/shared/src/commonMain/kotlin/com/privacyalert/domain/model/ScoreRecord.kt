package com.privacyalert.domain.model

import kotlinx.datetime.Instant

data class ScoreRecord(
    val id: String,
    val score: Int,
    val recordedAt: Instant,
)
