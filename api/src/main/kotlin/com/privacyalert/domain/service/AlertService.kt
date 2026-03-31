package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val scoreService: ScoreService,
) {

    fun findAll(
        userId: UUID,
        category: ThreatCategory?,
        severity: Severity?,
        pageable: Pageable,
    ): Page<Alert> = alertRepository.findAllByUserIdAndCategoryAndSeverity(userId, category, severity, pageable)

    fun findById(id: UUID, userId: UUID): Alert {
        val alert = alertRepository.findById(id)
            ?: throw AppException.ResourceNotFoundException("Alert", id)

        if (alert.userId != userId) {
            throw AppException.ResourceNotFoundException("Alert", id)
        }

        return alert
    }

    fun resolve(id: UUID, userId: UUID): Alert {
        val alert = findById(id, userId)

        val resolved = alert.copy(
            resolved = true,
            resolvedAt = Instant.now(),
        )

        val saved = alertRepository.save(resolved)
        scoreService.recalculate(userId)
        return saved
    }

    fun delete(id: UUID, userId: UUID) {
        findById(id, userId)
        alertRepository.deleteById(id)
        scoreService.recalculate(userId)
    }
}
