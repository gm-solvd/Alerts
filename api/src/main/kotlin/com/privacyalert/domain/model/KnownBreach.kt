package com.privacyalert.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class KnownBreach(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val domain: String? = null,
    val breachDate: LocalDate? = null,
    val dataClasses: List<String> = emptyList(),
    val recordCount: Long? = null,
    val sourceUrl: String? = null,
    val ingestedAt: Instant = Instant.now(),
)
