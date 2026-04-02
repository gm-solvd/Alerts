package com.privacyalert.data.remote.api

import com.privacyalert.data.remote.dto.AuthTokensResponseDto
import com.privacyalert.data.remote.dto.LoginRequestDto
import com.privacyalert.data.remote.dto.RefreshTokenRequestDto
import com.privacyalert.data.remote.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(private val client: HttpClient) {

    suspend fun register(request: RegisterRequestDto): AuthTokensResponseDto =
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun login(request: LoginRequestDto): AuthTokensResponseDto =
        client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun refresh(request: RefreshTokenRequestDto): AuthTokensResponseDto =
        client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
