package com.privacyalert.domain.usecase.mitigation

import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import com.privacyalert.domain.util.safeApiCall

class GetMitigationsUseCase(private val mitigationRepository: MitigationRepository) {
    suspend operator fun invoke(): Result<List<Mitigation>> =
        safeApiCall { mitigationRepository.getMitigations() }
}
