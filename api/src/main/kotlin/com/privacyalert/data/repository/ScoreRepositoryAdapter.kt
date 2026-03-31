package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.repository.ScoreRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ScoreRepositoryAdapter(
    private val jpa: ScoreHistoryJpaRepository,
) : ScoreRepository {

    override fun save(record: ScoreRecord): ScoreRecord =
        jpa.save(record.toEntity()).toDomain()

    override fun findLatestByUserId(userId: UUID): ScoreRecord? =
        jpa.findFirstByUserIdOrderByRecordedAtDesc(userId)?.toDomain()

    override fun findAllByUserId(userId: UUID, pageable: Pageable): Page<ScoreRecord> =
        jpa.findAllByUserId(userId, pageable).map { it.toDomain() }
}
