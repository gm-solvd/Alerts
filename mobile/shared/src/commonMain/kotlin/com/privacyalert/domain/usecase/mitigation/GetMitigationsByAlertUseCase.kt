package com.privacyalert.domain.usecase.mitigation

import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import com.privacyalert.domain.util.safeApiCall

class GetMitigationsByAlertUseCase(private val mitigationRepository: MitigationRepository) {
    suspend operator fun invoke(alertId: String): Result<List<Mitigation>> =
        safeApiCall { mitigationRepository.getMitigationsByAlertId(alertId) }
}
