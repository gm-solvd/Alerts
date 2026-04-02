package com.privacyalert.data.repository

import com.privacyalert.data.remote.api.AlertApi
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.PageResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository

class AlertRepositoryImpl(
    private val alertApi: AlertApi,
) : AlertRepository {

    override suspend fun getAlerts(
        category: ThreatCategory?,
        severity: Severity?,
        page: Int,
        size: Int,
    ): PageResult<Alert> =
        alertApi.getAlerts(
            category = category?.name,
            severity = severity?.name,
            page = page,
            size = size,
        ).toDomain { it.toDomain() }

    override suspend fun getAlertById(id: String): Alert =
        alertApi.getAlertById(id).toDomain()

    override suspend fun resolveAlert(id: String): Alert =
        alertApi.resolveAlert(id).toDomain()
}
