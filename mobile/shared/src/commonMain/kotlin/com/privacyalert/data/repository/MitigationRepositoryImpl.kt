package com.privacyalert.data.repository

import com.privacyalert.data.remote.api.MitigationApi
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository

class MitigationRepositoryImpl(
    private val mitigationApi: MitigationApi,
) : MitigationRepository {

    override suspend fun getMitigations(): List<Mitigation> =
        mitigationApi.getMitigations().map { it.toDomain() }

    override suspend fun getMitigationsByAlertId(alertId: String): List<Mitigation> =
        mitigationApi.getMitigationsByAlertId(alertId).map { it.toDomain() }

    override suspend fun completeMitigation(id: String): Mitigation =
        mitigationApi.completeMitigation(id).toDomain()
}
