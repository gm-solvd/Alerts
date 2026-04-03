package com.privacyalert.domain.usecase.alert

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.util.safeFlow
import kotlinx.coroutines.flow.Flow

class GetAlertsUseCase(private val alertRepository: AlertRepository) {
    operator fun invoke(
        category: ThreatCategory? = null,
        severity: Severity? = null,
        page: Int = 0,
        size: Int = 20,
    ): Flow<Result<PageResult<Alert>>> =
        safeFlow { alertRepository.getAlerts(category, severity, page, size) }
}
