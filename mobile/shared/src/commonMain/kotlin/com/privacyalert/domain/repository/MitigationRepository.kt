package com.privacyalert.domain.repository

import com.privacyalert.domain.model.Mitigation

interface MitigationRepository {
    suspend fun getMitigations(): List<Mitigation>
    suspend fun getMitigationsByAlertId(alertId: String): List<Mitigation>
    suspend fun completeMitigation(id: String): Mitigation
}
