package com.privacyalert.data.remote.api

import com.privacyalert.data.remote.dto.AlertResponseDto
import com.privacyalert.data.remote.dto.ScanProfileRequestDto
import com.privacyalert.data.remote.throwIfError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ScanApi(private val client: HttpClient) {

    suspend fun fullScan(request: ScanProfileRequestDto): List<AlertResponseDto> {
        val response = client.post("/api/v1/scan/full") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.throwIfError()
        return response.body()
    }

    suspend fun breachScan(email: String): List<AlertResponseDto> {
        val response = client.post("/api/v1/scan/breach") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }
        response.throwIfError()
        return response.body()
    }
}
