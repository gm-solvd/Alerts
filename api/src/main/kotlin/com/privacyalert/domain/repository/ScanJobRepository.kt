package com.privacyalert.domain.repository

import com.privacyalert.domain.model.ScanJob
import java.util.UUID

interface ScanJobRepository {
    fun save(job: ScanJob): ScanJob

    fun findById(id: UUID): ScanJob?

    fun findLatestByUserId(userId: UUID): ScanJob?
}
