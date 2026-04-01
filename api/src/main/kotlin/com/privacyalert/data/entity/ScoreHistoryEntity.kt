package com.privacyalert.data.entity

import com.privacyalert.domain.model.ScoreRecord
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
@Table(name = "score_history")
class ScoreHistoryEntity(
    @Id
    @get:JvmName("getEntityId")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val score: Short = 100,
    @Column(nullable = false)
    val recordedAt: Instant = Instant.now(),
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

fun ScoreHistoryEntity.toDomain(): ScoreRecord =
    ScoreRecord(
        id = id,
        userId = userId,
        score = score.toInt(),
        recordedAt = recordedAt,
    )

fun ScoreRecord.toEntity(): ScoreHistoryEntity =
    ScoreHistoryEntity(
        id = id,
        userId = userId,
        score = score.toShort(),
        recordedAt = recordedAt,
    )
