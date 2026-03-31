package com.privacyalert.domain.repository

import com.privacyalert.domain.model.RefreshToken
import java.util.UUID

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken

    fun findByTokenHash(tokenHash: String): RefreshToken?

    fun revokeAllByUserId(userId: UUID)
}
