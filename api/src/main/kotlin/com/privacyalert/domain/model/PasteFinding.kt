package com.privacyalert.domain.model

import java.time.Instant
import java.util.UUID

data class PasteFinding(
    val id: UUID = UUID.randomUUID(),
    val source: String,
    val pasteUrl: String,
    val title: String? = null,
    val snippet: String? = null,
    val discoveredAt: Instant = Instant.now(),
)
