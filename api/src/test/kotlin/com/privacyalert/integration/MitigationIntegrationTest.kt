package com.privacyalert.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import java.util.UUID

@Sql(
    scripts = ["/sql/cleanup.sql", "/sql/common-fixtures.sql", "/sql/alerts-fixtures.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/sql/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
class MitigationIntegrationTest : BaseIntegrationTest() {
    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        val tokens = loginUser("user@test.com", "password123")
        accessToken = tokens.accessToken
    }

    @Test
    fun `list mitigations returns all for authenticated user`() {
        val response =
            get(
                "/api/v1/mitigations",
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isEqualTo(2)
    }

    @Test
    fun `get mitigations by alert id`() {
        val response =
            get(
                "/api/v1/mitigations/aaaa1111-0000-0000-0000-000000000001",
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isEqualTo(2)

        body.forEach { mitigation ->
            assertThat(mitigation.get("alertId").asText())
                .isEqualTo("aaaa1111-0000-0000-0000-000000000001")
            assertThat(mitigation.get("id").asText()).isNotBlank()
            assertThat(mitigation.get("title").asText()).isNotBlank()
            assertThat(mitigation.get("description").asText()).isNotBlank()
            assertThat(mitigation.get("completed").asBoolean()).isFalse()
            assertThat(mitigation.get("completedAt").isNull).isTrue()
            assertThat(mitigation.get("createdAt").asText()).isNotBlank()
        }
    }

    @Test
    fun `complete mitigation sets completed flag and timestamp`() {
        val response =
            patch(
                "/api/v1/mitigations/bbbb1111-0000-0000-0000-000000000001/complete",
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("id").asText()).isEqualTo("bbbb1111-0000-0000-0000-000000000001")
        assertThat(body.get("completed").asBoolean()).isTrue()
        assertThat(body.get("completedAt").isNull).isFalse()
    }

    @Test
    fun `complete mitigation returns 404 for unknown id`() {
        val unknownId = UUID.randomUUID()
        val response =
            patch(
                "/api/v1/mitigations/$unknownId/complete",
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `list mitigations returns 403 without auth`() {
        val response =
            get(
                "/api/v1/mitigations",
                org.springframework.http.HttpHeaders(),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
