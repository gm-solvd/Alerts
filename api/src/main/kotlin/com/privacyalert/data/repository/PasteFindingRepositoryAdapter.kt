package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.PasteFinding
import com.privacyalert.domain.repository.PasteFindingRepository
import org.springframework.stereotype.Repository

@Repository
class PasteFindingRepositoryAdapter(
    private val jpa: PasteFindingJpaRepository,
) : PasteFindingRepository {

    override fun existsByPasteUrl(pasteUrl: String): Boolean =
        jpa.existsByPasteUrl(pasteUrl)

    override fun save(finding: PasteFinding): PasteFinding =
        jpa.save(finding.toEntity()).toDomain()
}
