package com.privacyalert.data.remote.api

import com.privacyalert.data.remote.dto.AlertResponseDto
import com.privacyalert.data.remote.dto.PageResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch

class AlertApi(private val client: HttpClient) {

    suspend fun getAlerts(
        category: String? = null,
        severity: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): PageResponseDto<AlertResponseDto> =
        client.get("/api/v1/alerts") {
            parameter("page", page)
            parameter("size", size)
            category?.let { parameter("category", it) }
            severity?.let { parameter("severity", it) }
        }.body()

    suspend fun getAlertById(id: String): AlertResponseDto =
        client.get("/api/v1/alerts/$id").body()

    suspend fun resolveAlert(id: String): AlertResponseDto =
        client.patch("/api/v1/alerts/$id/resolve").body()
}
