package com.privacyalert.domain.service

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class MitigationService(
    private val mitigationRepository: MitigationRepository,
) {
    fun findAllByUserId(userId: UUID): List<Mitigation> = mitigationRepository.findAllByUserId(userId)

    fun findByAlertId(alertId: UUID): List<Mitigation> = mitigationRepository.findAllByAlertId(alertId)

    fun complete(id: UUID): Mitigation {
        val mitigation =
            mitigationRepository.findById(id)
                ?: throw AppException.ResourceNotFoundException("Mitigation", id)

        return mitigationRepository.save(
            mitigation.copy(
                completed = true,
                completedAt = Instant.now(),
            ),
        )
    }
}
