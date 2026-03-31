package com.privacyalert.domain.service

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.RefreshToken
import com.privacyalert.domain.model.User
import com.privacyalert.domain.repository.RefreshTokenRepository
import com.privacyalert.domain.repository.UserRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtProvider = mockk<JwtProvider>()
    private val oAuthVerifier = mockk<OAuthVerifier>()

    private val service = AuthService(
        userRepository,
        refreshTokenRepository,
        passwordEncoder,
        jwtProvider,
        oAuthVerifier,
    )

    private val testUser = User(
        id = UUID.randomUUID(),
        email = "test@example.com",
        passwordHash = "hashed-password",
    )

    @Test
    fun `register creates user and returns tokens`() {
        every { userRepository.existsByEmail("new@example.com") } returns false
        every { passwordEncoder.hash("password123") } returns "hashed"
        every { userRepository.save(any()) } answers { firstArg() }
        every { jwtProvider.generateAccessToken(any(), any()) } returns "access-token"
        every { jwtProvider.generateRefreshToken() } returns "refresh-token"
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val tokens = service.register("new@example.com", "password123")

        assertEquals("access-token", tokens.accessToken)
        assertEquals("refresh-token", tokens.refreshToken)
        verify { userRepository.save(match { it.email == "new@example.com" }) }
    }

    @Test
    fun `register throws ConflictException when email already exists`() {
        every { userRepository.existsByEmail("existing@example.com") } returns true

        assertThrows<AppException.ConflictException> {
            service.register("existing@example.com", "password123")
        }
    }

    @Test
    fun `login returns tokens for valid credentials`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        every { passwordEncoder.matches("password123", "hashed-password") } returns true
        every { jwtProvider.generateAccessToken(testUser.id, testUser.email) } returns "access-token"
        every { jwtProvider.generateRefreshToken() } returns "refresh-token"
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val tokens = service.login("test@example.com", "password123")

        assertEquals("access-token", tokens.accessToken)
        assertEquals("refresh-token", tokens.refreshToken)
    }

    @Test
    fun `login throws UnauthorizedException when email not found`() {
        every { userRepository.findByEmail("unknown@example.com") } returns null

        assertThrows<AppException.UnauthorizedException> {
            service.login("unknown@example.com", "password123")
        }
    }

    @Test
    fun `login throws UnauthorizedException when password does not match`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        every { passwordEncoder.matches("wrong-password", "hashed-password") } returns false

        assertThrows<AppException.UnauthorizedException> {
            service.login("test@example.com", "wrong-password")
        }
    }

    @Test
    fun `login throws UnauthorizedException when user has no password hash`() {
        val oauthUser = testUser.copy(passwordHash = null)
        every { userRepository.findByEmail("test@example.com") } returns oauthUser

        assertThrows<AppException.UnauthorizedException> {
            service.login("test@example.com", "password123")
        }
    }

    @Test
    fun `refreshToken rotates tokens for valid refresh token`() {
        val storedToken = RefreshToken(
            userId = testUser.id,
            tokenHash = "hashed-refresh",
            expiresAt = Instant.now().plus(29, ChronoUnit.DAYS),
        )

        every { refreshTokenRepository.findByTokenHash(any()) } returns storedToken
        every { refreshTokenRepository.revokeAllByUserId(testUser.id) } just Runs
        every { userRepository.findById(testUser.id) } returns testUser
        every { jwtProvider.generateAccessToken(testUser.id, testUser.email) } returns "new-access"
        every { jwtProvider.generateRefreshToken() } returns "new-refresh"
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val tokens = service.refreshToken("raw-refresh-token")

        assertEquals("new-access", tokens.accessToken)
        assertEquals("new-refresh", tokens.refreshToken)
        verify { refreshTokenRepository.revokeAllByUserId(testUser.id) }
    }

    @Test
    fun `refreshToken throws UnauthorizedException when token not found`() {
        every { refreshTokenRepository.findByTokenHash(any()) } returns null

        assertThrows<AppException.UnauthorizedException> {
            service.refreshToken("invalid-token")
        }
    }

    @Test
    fun `refreshToken throws UnauthorizedException when token is revoked`() {
        val revokedToken = RefreshToken(
            userId = testUser.id,
            tokenHash = "hashed",
            expiresAt = Instant.now().plus(29, ChronoUnit.DAYS),
            revoked = true,
        )

        every { refreshTokenRepository.findByTokenHash(any()) } returns revokedToken

        assertThrows<AppException.UnauthorizedException> {
            service.refreshToken("revoked-token")
        }
    }

    @Test
    fun `refreshToken throws UnauthorizedException when token is expired`() {
        val expiredToken = RefreshToken(
            userId = testUser.id,
            tokenHash = "hashed",
            expiresAt = Instant.now().minus(1, ChronoUnit.DAYS),
        )

        every { refreshTokenRepository.findByTokenHash(any()) } returns expiredToken

        assertThrows<AppException.UnauthorizedException> {
            service.refreshToken("expired-token")
        }
    }

    @Test
    fun `oauthCallback returns tokens for existing OAuth user`() {
        val oauthInfo = OAuthUserInfo(provider = "google", subject = "sub123", email = "oauth@example.com")
        val oauthUser = testUser.copy(oauthProvider = "google", oauthSubject = "sub123")

        every { oAuthVerifier.verify("google", "id-token") } returns oauthInfo
        every { userRepository.findByOauthProviderAndOauthSubject("google", "sub123") } returns oauthUser
        every { jwtProvider.generateAccessToken(oauthUser.id, oauthUser.email) } returns "access"
        every { jwtProvider.generateRefreshToken() } returns "refresh"
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val tokens = service.oauthCallback("google", "id-token")

        assertNotNull(tokens)
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `oauthCallback creates new user when OAuth user not found`() {
        val oauthInfo = OAuthUserInfo(provider = "google", subject = "new-sub", email = "new@example.com")

        every { oAuthVerifier.verify("google", "id-token") } returns oauthInfo
        every { userRepository.findByOauthProviderAndOauthSubject("google", "new-sub") } returns null
        every { userRepository.save(any()) } answers { firstArg() }
        every { jwtProvider.generateAccessToken(any(), any()) } returns "access"
        every { jwtProvider.generateRefreshToken() } returns "refresh"
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val tokens = service.oauthCallback("google", "id-token")

        assertNotNull(tokens)
        verify { userRepository.save(match { it.oauthProvider == "google" && it.oauthSubject == "new-sub" }) }
    }
}
