package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.RefreshToken
import com.privacyalert.domain.repository.RefreshTokenRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class RefreshTokenRepositoryAdapter(
    private val jpa: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(token: RefreshToken): RefreshToken = jpa.save(token.toEntity()).toDomain()

    override fun findByTokenHash(tokenHash: String): RefreshToken? = jpa.findByTokenHash(tokenHash)?.toDomain()

    @Transactional
    override fun revokeAllByUserId(userId: UUID) = jpa.revokeAllByUserId(userId)
}
