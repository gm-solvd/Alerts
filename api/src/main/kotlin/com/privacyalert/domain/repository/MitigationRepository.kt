package com.privacyalert.domain.repository

import com.privacyalert.domain.model.Mitigation
import java.util.UUID

interface MitigationRepository {
    fun findById(id: UUID): Mitigation?

    fun findAllByAlertId(alertId: UUID): List<Mitigation>

    fun findAllByUserId(userId: UUID): List<Mitigation>

    fun save(mitigation: Mitigation): Mitigation
}
