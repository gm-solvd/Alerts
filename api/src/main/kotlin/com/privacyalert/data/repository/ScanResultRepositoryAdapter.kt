package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.ScanResult
import com.privacyalert.domain.repository.ScanResultRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ScanResultRepositoryAdapter(
    private val jpa: ScanResultJpaRepository,
) : ScanResultRepository {

    override fun save(result: ScanResult): ScanResult =
        jpa.save(result.toEntity()).toDomain()

    override fun findByUserIdAndScanType(userId: UUID, scanType: String): List<ScanResult> =
        jpa.findAllByUserIdAndScanType(userId, scanType).map { it.toDomain() }
}
