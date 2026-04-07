package com.privacyalert.domain.service

import com.privacyalert.domain.model.DataBrokerCategory
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.UserScanProfile
import java.util.UUID

data class DataBrokerExposureResult(
    val brokerId: UUID,
    val brokerName: String,
    val category: DataBrokerCategory,
    val severity: Severity,
    val exposedFields: List<String>,
    val detectionMethod: String,
)

interface DataBrokerScanner {
    fun scan(profile: UserScanProfile): List<DataBrokerExposureResult>
}
