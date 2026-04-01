package com.privacyalert.data.repository

import com.privacyalert.data.entity.ScanJobEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ScanJobJpaRepository : JpaRepository<ScanJobEntity, UUID> {
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: UUID): ScanJobEntity?
}
