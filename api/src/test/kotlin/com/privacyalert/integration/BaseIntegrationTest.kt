package com.privacyalert.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.api.dto.AuthTokensResponse
import com.privacyalert.api.dto.LoginRequest
import com.privacyalert.api.dto.RegisterRequest
import com.privacyalert.domain.service.BreachScanner
import com.privacyalert.domain.service.IdentityExposureScanner
import com.privacyalert.domain.service.OAuthVerifier
import com.privacyalert.domain.service.PiiExposureScanner
import com.privacyalert.domain.service.SocialFootprintScanner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
abstract class BaseIntegrationTest {
    companion object {
        @JvmStatic
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16").also { it.start() }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var breachScanner: BreachScanner

    @MockkBean
    lateinit var identityExposureScanner: IdentityExposureScanner

    @MockkBean
    lateinit var piiExposureScanner: PiiExposureScanner

    @MockkBean
    lateinit var socialFootprintScanner: SocialFootprintScanner

    @MockkBean
    lateinit var oAuthVerifier: OAuthVerifier

    protected fun registerUser(
        email: String,
        password: String,
    ): AuthTokensResponse {
        val request = RegisterRequest(email = email, password = password)
        val response =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                AuthTokensResponse::class.java,
            )
        return response.body!!
    }

    protected fun loginUser(
        email: String,
        password: String,
    ): AuthTokensResponse {
        val request = LoginRequest(email = email, password = password)
        val response =
            restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                AuthTokensResponse::class.java,
            )
        return response.body!!
    }

    protected fun adminHeaders(): HttpHeaders =
        HttpHeaders().apply {
            setBearerAuth("test-admin-token")
            contentType = MediaType.APPLICATION_JSON
        }

    protected fun userHeaders(accessToken: String): HttpHeaders =
        HttpHeaders().apply {
            setBearerAuth(accessToken)
            contentType = MediaType.APPLICATION_JSON
        }

    protected fun <T> get(
        url: String,
        headers: HttpHeaders,
        responseType: Class<T>,
    ): org.springframework.http.ResponseEntity<T> =
        restTemplate.exchange(
            url,
            org.springframework.http.HttpMethod.GET,
            HttpEntity<Void>(headers),
            responseType,
        )

    protected fun <T> post(
        url: String,
        body: Any?,
        headers: HttpHeaders,
        responseType: Class<T>,
    ): org.springframework.http.ResponseEntity<T> =
        restTemplate.exchange(
            url,
            org.springframework.http.HttpMethod.POST,
            HttpEntity(body, headers),
            responseType,
        )

    protected fun <T> patch(
        url: String,
        headers: HttpHeaders,
        responseType: Class<T>,
    ): org.springframework.http.ResponseEntity<T> =
        restTemplate.exchange(
            url,
            org.springframework.http.HttpMethod.PATCH,
            HttpEntity<Void>(headers),
            responseType,
        )

    protected fun delete(
        url: String,
        headers: HttpHeaders,
    ): org.springframework.http.ResponseEntity<Void> =
        restTemplate.exchange(
            url,
            org.springframework.http.HttpMethod.DELETE,
            HttpEntity<Void>(headers),
            Void::class.java,
        )
}
