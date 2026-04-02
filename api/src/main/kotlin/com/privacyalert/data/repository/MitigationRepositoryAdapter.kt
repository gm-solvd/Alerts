package com.privacyalert.data.repository

import com.privacyalert.data.entity.toDomain
import com.privacyalert.data.entity.toEntity
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MitigationRepositoryAdapter(
    private val jpa: MitigationJpaRepository,
) : MitigationRepository {
    override fun findById(id: UUID): Mitigation? = jpa.findById(id).orElse(null)?.toDomain()

    override fun findAllByAlertId(alertId: UUID): List<Mitigation> = jpa.findAllByAlertId(alertId).map { it.toDomain() }

    override fun findAllByUserId(userId: UUID): List<Mitigation> = jpa.findAllByUserId(userId).map { it.toDomain() }

    override fun save(mitigation: Mitigation): Mitigation {
        val entity = mitigation.toEntity()
        if (jpa.existsById(mitigation.id)) {
            entity.markNotNew()
        }
        return jpa.save(entity).toDomain()
    }
}
