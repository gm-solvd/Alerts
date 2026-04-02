package com.privacyalert.data.repository

import com.privacyalert.data.remote.api.ScanApi
import com.privacyalert.data.remote.dto.ScanProfileRequestDto
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.repository.ScanRepository

class ScanRepositoryImpl(
    private val scanApi: ScanApi,
) : ScanRepository {

    override suspend fun fullScan(email: String): List<Alert> =
        scanApi.fullScan(ScanProfileRequestDto(email = email)).map { it.toDomain() }

    override suspend fun breachScan(email: String): List<Alert> =
        scanApi.breachScan(email).map { it.toDomain() }
}
