package com.privacyalert.data.entity

import com.privacyalert.domain.model.ScoreRecord
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "score_history")
class ScoreHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val score: Short = 100,

    @Column(nullable = false)
    val recordedAt: Instant = Instant.now(),
)

fun ScoreHistoryEntity.toDomain(): ScoreRecord = ScoreRecord(
    id = id,
    userId = userId,
    score = score.toInt(),
    recordedAt = recordedAt,
)

fun ScoreRecord.toEntity(): ScoreHistoryEntity = ScoreHistoryEntity(
    id = id,
    userId = userId,
    score = score.toShort(),
    recordedAt = recordedAt,
)
