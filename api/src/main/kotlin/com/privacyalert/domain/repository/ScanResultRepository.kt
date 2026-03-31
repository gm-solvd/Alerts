package com.privacyalert.domain.repository

import com.privacyalert.domain.model.ScanResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ScanResultRepository {
    fun save(result: ScanResult): ScanResult

    fun findByUserIdAndScanType(
        userId: UUID,
        scanType: String,
    ): List<ScanResult>

    fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<ScanResult>
}
