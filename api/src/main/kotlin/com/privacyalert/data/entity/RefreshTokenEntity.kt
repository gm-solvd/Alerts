package com.privacyalert.data.entity

import com.privacyalert.domain.model.RefreshToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val tokenHash: String = "",
    @Column(nullable = false)
    val expiresAt: Instant = Instant.now(),
    @Column(nullable = false)
    var revoked: Boolean = false,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
) : Persistable<UUID> {
    @Transient
    private var new: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = new

    @PostLoad
    @PostPersist
    fun markNotNew() {
        new = false
    }
}

fun RefreshTokenEntity.toDomain(): RefreshToken =
    RefreshToken(
        id = id,
        userId = userId,
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        revoked = revoked,
        createdAt = createdAt,
    )

fun RefreshToken.toEntity(): RefreshTokenEntity =
    RefreshTokenEntity(
        id = id,
        userId = userId,
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        revoked = revoked,
        createdAt = createdAt,
    )
