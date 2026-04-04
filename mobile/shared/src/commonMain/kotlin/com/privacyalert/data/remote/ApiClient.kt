package com.privacyalert.data.remote

import com.privacyalert.data.local.TokenStorage
import com.privacyalert.data.remote.dto.AuthTokensResponseDto
import com.privacyalert.data.remote.dto.RefreshTokenRequestDto
import com.privacyalert.domain.model.AuthTokens
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import com.privacyalert.isDebugBuild
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {

    @Suppress("LongMethod")
    fun create(tokenStorage: TokenStorage): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                    encodeDefaults = true
                },
            )
        }

        install(Logging) {
            logger = httpLogger
            level = if (isDebugBuild) LogLevel.ALL else LogLevel.NONE
        }

        installResponseValidator()

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenStorage.getAccessToken()
                    val refresh = tokenStorage.getRefreshToken()
                    if (access != null && refresh != null) {
                        BearerTokens(access, refresh)
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val refresh = tokenStorage.getRefreshToken() ?: return@refreshTokens null
                    try {
                        val response = client.post("/api/v1/auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequestDto(refreshToken = refresh))
                            markAsRefreshTokenRequest()
                        }.body<AuthTokensResponseDto>()

                        tokenStorage.saveTokens(
                            AuthTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken,
                            ),
                        )

                        BearerTokens(response.accessToken, response.refreshToken)
                    } catch (_: Exception) {
                        tokenStorage.clearTokens()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    !request.url.pathSegments.joinToString("/").contains("auth")
                }
            }
        }

        defaultRequest {
            url(ApiConstants.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }

    private fun HttpClientConfig<*>.installResponseValidator() {
        HttpResponseValidator {
            validateResponse { response ->
                statusToAppError(response.status)?.let { throw it }
            }
        }
    }
}
