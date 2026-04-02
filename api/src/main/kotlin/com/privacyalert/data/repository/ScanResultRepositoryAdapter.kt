package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.ScanResult
import com.privacyalert.domain.repository.ScanResultRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ScanResultRepositoryAdapter(
    private val jpa: ScanResultJpaRepository,
) : ScanResultRepository {
    override fun save(result: ScanResult): ScanResult {
        val entity = result.toEntity()
        if (jpa.existsById(result.id)) {
            entity.markNotNew()
        }
        return jpa.save(entity).toDomain()
    }

    override fun findByUserIdAndScanType(
        userId: UUID,
        scanType: String,
    ): List<ScanResult> = jpa.findAllByUserIdAndScanType(userId, scanType).map { it.toDomain() }

    override fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<ScanResult> = jpa.findAllByUserIdOrderByCreatedAtDesc(userId, pageable).map { it.toDomain() }
}
