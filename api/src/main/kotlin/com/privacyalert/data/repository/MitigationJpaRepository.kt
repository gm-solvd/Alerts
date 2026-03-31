package com.privacyalert.data.repository

import com.privacyalert.data.entity.MitigationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface MitigationJpaRepository : JpaRepository<MitigationEntity, UUID> {

    fun findAllByAlertId(alertId: UUID): List<MitigationEntity>

    @Query(
        """
        SELECT m FROM MitigationEntity m
        JOIN AlertEntity a ON m.alertId = a.id
        WHERE a.userId = :userId
        """,
    )
    fun findAllByUserId(userId: UUID): List<MitigationEntity>
}
