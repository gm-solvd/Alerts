package com.privacyalert.data.repository

import com.privacyalert.data.entity.ScoreHistoryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ScoreHistoryJpaRepository : JpaRepository<ScoreHistoryEntity, UUID> {
    fun findFirstByUserIdOrderByRecordedAtDesc(userId: UUID): ScoreHistoryEntity?

    fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<ScoreHistoryEntity>
}
