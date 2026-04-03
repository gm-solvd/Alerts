package com.privacyalert.domain.usecase.mitigation

import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import com.privacyalert.domain.util.safeFlow
import kotlinx.coroutines.flow.Flow

class GetMitigationsUseCase(private val mitigationRepository: MitigationRepository) {
    operator fun invoke(): Flow<Result<List<Mitigation>>> =
        safeFlow { mitigationRepository.getMitigations() }
}
