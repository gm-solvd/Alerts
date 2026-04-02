package com.privacyalert.domain.repository

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory

interface AlertRepository {
    suspend fun getAlerts(
        category: ThreatCategory? = null,
        severity: Severity? = null,
        page: Int = 0,
        size: Int = 20,
    ): PageResult<Alert>

    suspend fun getAlertById(id: String): Alert
    suspend fun resolveAlert(id: String): Alert
}
