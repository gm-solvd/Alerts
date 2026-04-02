package com.privacyalert.domain.usecase.mitigation

import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import com.privacyalert.domain.util.safeApiCall

class CompleteMitigationUseCase(private val mitigationRepository: MitigationRepository) {
    suspend operator fun invoke(id: String): Result<Mitigation> =
        safeApiCall { mitigationRepository.completeMitigation(id) }
}
