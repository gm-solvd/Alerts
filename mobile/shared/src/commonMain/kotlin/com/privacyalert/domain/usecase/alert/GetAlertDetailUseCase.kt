package com.privacyalert.domain.usecase.alert

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.util.safeApiCall

class GetAlertDetailUseCase(private val alertRepository: AlertRepository) {
    suspend operator fun invoke(id: String): Result<Alert> =
        safeApiCall { alertRepository.getAlertById(id) }
}
