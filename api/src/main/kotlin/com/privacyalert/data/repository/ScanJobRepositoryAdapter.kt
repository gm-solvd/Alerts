package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.ScanJob
import com.privacyalert.domain.repository.ScanJobRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ScanJobRepositoryAdapter(
    private val jpa: ScanJobJpaRepository,
) : ScanJobRepository {
    override fun save(job: ScanJob): ScanJob {
        val entity = job.toEntity()
        if (jpa.existsById(job.id)) {
            entity.markNotNew()
        }
        return jpa.save(entity).toDomain()
    }

    override fun findById(id: UUID): ScanJob? = jpa.findById(id).map { it.toDomain() }.orElse(null)

    override fun findLatestByUserId(userId: UUID): ScanJob? = jpa.findFirstByUserIdOrderByCreatedAtDesc(userId)?.toDomain()
}
