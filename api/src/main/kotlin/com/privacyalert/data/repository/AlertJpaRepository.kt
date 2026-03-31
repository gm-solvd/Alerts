package com.privacyalert.data.repository

import com.privacyalert.data.entity.AlertEntity
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface AlertJpaRepository : JpaRepository<AlertEntity, UUID> {
    fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<AlertEntity>

    @Query(
        """
        SELECT a FROM AlertEntity a
        WHERE a.userId = :userId
        AND (:category IS NULL OR a.category = :category)
        AND (:severity IS NULL OR a.severity = :severity)
        """,
    )
    fun findAllByUserIdAndOptionalFilters(
        userId: UUID,
        category: ThreatCategory?,
        severity: Severity?,
        pageable: Pageable,
    ): Page<AlertEntity>

    fun findAllByUserIdAndResolvedFalse(userId: UUID): List<AlertEntity>
}
