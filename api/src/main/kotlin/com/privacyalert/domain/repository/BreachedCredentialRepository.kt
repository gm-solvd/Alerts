package com.privacyalert.domain.repository

import java.util.UUID

interface BreachedCredentialRepository {
    fun existsByEmailHashAndBreachId(
        emailSha256: String,
        breachId: UUID,
    ): Boolean

    fun saveCredential(
        emailSha256: String,
        breachId: UUID,
    )
}
