package com.privacyalert.domain.usecase.alert

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.util.safeApiCall

class GetAlertsUseCase(private val alertRepository: AlertRepository) {
    suspend operator fun invoke(
        category: ThreatCategory? = null,
        severity: Severity? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<PageResult<Alert>> =
        safeApiCall { alertRepository.getAlerts(category, severity, page, size) }
}
