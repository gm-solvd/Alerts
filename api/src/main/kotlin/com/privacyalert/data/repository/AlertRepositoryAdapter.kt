package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AlertRepositoryAdapter(
    private val jpa: AlertJpaRepository,
) : AlertRepository {
    override fun findById(id: UUID): Alert? = jpa.findById(id).orElse(null)?.toDomain()

    override fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<Alert> = jpa.findAllByUserId(userId, pageable).map { it.toDomain() }

    override fun findAllByUserIdAndCategoryAndSeverity(
        userId: UUID,
        category: ThreatCategory?,
        severity: Severity?,
        pageable: Pageable,
    ): Page<Alert> = jpa.findAllByUserIdAndOptionalFilters(userId, category, severity, pageable).map { it.toDomain() }

    override fun findAllUnresolvedByUserId(userId: UUID): List<Alert> = jpa.findAllByUserIdAndResolvedFalse(userId).map { it.toDomain() }

    override fun save(alert: Alert): Alert {
        val entity = alert.toEntity()
        if (jpa.existsById(alert.id)) {
            entity.markNotNew()
        }
        return jpa.save(entity).toDomain()
    }

    override fun deleteById(id: UUID) = jpa.deleteById(id)

    override fun countByUserId(userId: UUID): Long = jpa.countByUserId(userId)

    override fun count(): Long = jpa.count()

    override fun countByCategory(): Map<ThreatCategory, Long> =
        jpa.countGroupByCategory().associate { row ->
            (row[0] as ThreatCategory) to (row[1] as Long)
        }

    override fun countBySeverity(): Map<Severity, Long> =
        jpa.countGroupBySeverity().associate { row ->
            (row[0] as Severity) to (row[1] as Long)
        }
}
