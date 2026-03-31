package com.privacyalert.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.UUID

class JwtProviderImplTest {

    private val properties = AppProperties(
        jwt = AppProperties.JwtProperties(
            secret = "this-is-a-very-long-secret-key-for-hs256-at-least-32-bytes",
            accessTokenTtl = Duration.ofMinutes(15),
            refreshTokenTtl = Duration.ofDays(30),
        ),
        hibp = AppProperties.HibpProperties(),
    )

    private val jwtProvider = JwtProviderImpl(properties)

    @Test
    fun `generateAccessToken returns a valid JWT`() {
        val userId = UUID.randomUUID()
        val token = jwtProvider.generateAccessToken(userId, "test@example.com")

        assertNotNull(token)
        val extractedId = jwtProvider.validateAndExtractUserId(token)
        assertEquals(userId, extractedId)
    }

    @Test
    fun `validateAndExtractUserId returns null for invalid token`() {
        val result = jwtProvider.validateAndExtractUserId("invalid-token")
        assertNull(result)
    }

    @Test
    fun `validateAndExtractUserId returns null for token signed with different key`() {
        val otherProperties = properties.copy(
            jwt = properties.jwt.copy(secret = "different-secret-key-that-is-also-at-least-32-bytes-long"),
        )
        val otherProvider = JwtProviderImpl(otherProperties)

        val userId = UUID.randomUUID()
        val token = otherProvider.generateAccessToken(userId, "test@example.com")

        val result = jwtProvider.validateAndExtractUserId(token)
        assertNull(result)
    }

    @Test
    fun `generateRefreshToken returns a UUID string`() {
        val token = jwtProvider.generateRefreshToken()
        assertNotNull(token)
        // Should be parseable as UUID
        UUID.fromString(token)
    }

    @Test
    fun `generateAccessToken produces unique tokens for different users`() {
        val token1 = jwtProvider.generateAccessToken(UUID.randomUUID(), "user1@example.com")
        val token2 = jwtProvider.generateAccessToken(UUID.randomUUID(), "user2@example.com")
        org.junit.jupiter.api.Assertions.assertNotEquals(token1, token2)
    }
}
