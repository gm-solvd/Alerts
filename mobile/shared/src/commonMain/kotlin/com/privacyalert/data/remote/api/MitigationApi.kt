package com.privacyalert.data.remote.api

import com.privacyalert.data.remote.dto.MitigationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch

class MitigationApi(private val client: HttpClient) {

    suspend fun getMitigations(): List<MitigationResponseDto> =
        client.get("/api/v1/mitigations").body()

    suspend fun getMitigationsByAlertId(alertId: String): List<MitigationResponseDto> =
        client.get("/api/v1/mitigations/$alertId").body()

    suspend fun completeMitigation(id: String): MitigationResponseDto =
        client.patch("/api/v1/mitigations/$id/complete").body()
}
