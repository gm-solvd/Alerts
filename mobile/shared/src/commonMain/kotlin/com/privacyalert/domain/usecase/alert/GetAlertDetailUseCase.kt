package com.privacyalert.domain.usecase.alert

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.util.safeFlow
import kotlinx.coroutines.flow.Flow

class GetAlertDetailUseCase(private val alertRepository: AlertRepository) {
    operator fun invoke(id: String): Flow<Result<Alert>> =
        safeFlow { alertRepository.getAlertById(id) }
}
