package com.privacyalert.data.repository

import com.privacyalert.data.entity.PasteFindingEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PasteFindingJpaRepository : JpaRepository<PasteFindingEntity, UUID> {

    fun existsByPasteUrl(pasteUrl: String): Boolean
}
