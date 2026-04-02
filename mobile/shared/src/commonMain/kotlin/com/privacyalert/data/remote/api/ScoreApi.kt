package com.privacyalert.data.remote.api

import com.privacyalert.data.remote.dto.PageResponseDto
import com.privacyalert.data.remote.dto.ScoreResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ScoreApi(private val client: HttpClient) {

    suspend fun getCurrentScore(): ScoreResponseDto =
        client.get("/api/v1/score").body()

    suspend fun getScoreHistory(
        page: Int = 0,
        size: Int = 20,
    ): PageResponseDto<ScoreResponseDto> =
        client.get("/api/v1/score/history") {
            parameter("page", page)
            parameter("size", size)
        }.body()
}
