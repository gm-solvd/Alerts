package com.privacyalert.domain.repository

import com.privacyalert.domain.model.ScanResult
import java.util.UUID

interface ScanResultRepository {
    fun save(result: ScanResult): ScanResult

    fun findByUserIdAndScanType(
        userId: UUID,
        scanType: String,
    ): List<ScanResult>
}
