package com.privacyalert.domain.repository

import com.privacyalert.domain.model.Alert

interface ScanRepository {
    suspend fun fullScan(email: String): List<Alert>
    suspend fun breachScan(email: String): List<Alert>
}
