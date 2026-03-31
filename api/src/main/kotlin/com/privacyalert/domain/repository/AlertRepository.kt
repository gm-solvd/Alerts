package com.privacyalert.domain.repository

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface AlertRepository {
    fun findById(id: UUID): Alert?

    fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<Alert>

    fun findAllByUserIdAndCategoryAndSeverity(
        userId: UUID,
        category: ThreatCategory?,
        severity: Severity?,
        pageable: Pageable,
    ): Page<Alert>

    fun findAllUnresolvedByUserId(userId: UUID): List<Alert>

    fun save(alert: Alert): Alert

    fun deleteById(id: UUID)
}
