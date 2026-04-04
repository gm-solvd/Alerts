package com.privacyalert.integration

import com.privacyalert.api.dto.PermissionAuditRequest
import com.privacyalert.api.dto.PermissionEntryDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql

@Sql(
    scripts = ["/sql/cleanup.sql", "/sql/common-fixtures.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/sql/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
class PermissionAuditIntegrationTest : BaseIntegrationTest() {
    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        val tokens = loginUser("user@test.com", "password123")
        accessToken = tokens.accessToken
    }

    @Test
    fun `submit risky permissions creates alerts`() {
        val request =
            PermissionAuditRequest(
                permissions =
                    listOf(
                        PermissionEntryDto(name = "android.permission.CAMERA", granted = true),
                        PermissionEntryDto(name = "android.permission.ACCESS_FINE_LOCATION", granted = true),
                    ),
            )

        val response =
            post(
                "/api/v1/audit/permissions",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isGreaterThanOrEqualTo(1)

        val hasOverpermissions = body.any { it.get("category").asText() == "APP_OVERPERMISSIONS" }
        assertThat(hasOverpermissions).isTrue()
    }

    @Test
    fun `submit safe permissions creates no alerts`() {
        val request =
            PermissionAuditRequest(
                permissions =
                    listOf(
                        PermissionEntryDto(name = "INTERNET", granted = true),
                    ),
            )

        val response =
            post(
                "/api/v1/audit/permissions",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isEqualTo(0)
    }

    @Test
    fun `permission audit returns 401 without auth`() {
        val request =
            PermissionAuditRequest(
                permissions =
                    listOf(
                        PermissionEntryDto(name = "CAMERA", granted = true),
                    ),
            )

        val response =
            post(
                "/api/v1/audit/permissions",
                request,
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                },
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
