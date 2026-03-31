package com.privacyalert.data.repository

import com.privacyalert.data.entity.ScanResultEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ScanResultJpaRepository : JpaRepository<ScanResultEntity, UUID> {
    fun findAllByUserIdAndScanType(
        userId: UUID,
        scanType: String,
    ): List<ScanResultEntity>

    fun findAllByUserIdOrderByCreatedAtDesc(
        userId: UUID,
        pageable: Pageable,
    ): Page<ScanResultEntity>
}
