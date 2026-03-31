package com.privacyalert.domain.service

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.AuthTokens
import com.privacyalert.domain.model.RefreshToken
import com.privacyalert.domain.model.User
import com.privacyalert.domain.repository.RefreshTokenRepository
import com.privacyalert.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val oAuthVerifier: OAuthVerifier,
) {

    fun register(email: String, password: String): AuthTokens {
        if (userRepository.existsByEmail(email)) {
            throw AppException.ConflictException("Email '$email' is already registered")
        }

        val user = userRepository.save(
            User(
                email = email,
                passwordHash = passwordEncoder.hash(password),
            ),
        )

        return issueTokens(user)
    }

    fun login(email: String, password: String): AuthTokens {
        val user = userRepository.findByEmail(email)
            ?: throw AppException.UnauthorizedException("Invalid email or password")

        if (user.passwordHash == null || !passwordEncoder.matches(password, user.passwordHash)) {
            throw AppException.UnauthorizedException("Invalid email or password")
        }

        return issueTokens(user)
    }

    fun refreshToken(rawRefreshToken: String): AuthTokens {
        val tokenHash = hashToken(rawRefreshToken)
        val stored = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw AppException.UnauthorizedException("Invalid refresh token")

        if (stored.revoked || stored.expiresAt.isBefore(Instant.now())) {
            throw AppException.UnauthorizedException("Refresh token expired or revoked")
        }

        // Rotate: revoke old, issue new
        refreshTokenRepository.revokeAllByUserId(stored.userId)

        val user = userRepository.findById(stored.userId)
            ?: throw AppException.ResourceNotFoundException("User", stored.userId)

        return issueTokens(user)
    }

    fun oauthCallback(provider: String, idToken: String): AuthTokens {
        val info = oAuthVerifier.verify(provider, idToken)

        val user = userRepository.findByOauthProviderAndOauthSubject(info.provider, info.subject)
            ?: userRepository.save(
                User(
                    email = info.email,
                    oauthProvider = info.provider,
                    oauthSubject = info.subject,
                ),
            )

        return issueTokens(user)
    }

    private fun issueTokens(user: User): AuthTokens {
        val accessToken = jwtProvider.generateAccessToken(user.id, user.email)
        val rawRefreshToken = jwtProvider.generateRefreshToken()

        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                tokenHash = hashToken(rawRefreshToken),
                expiresAt = Instant.now().plus(30, ChronoUnit.DAYS),
            ),
        )

        return AuthTokens(accessToken = accessToken, refreshToken = rawRefreshToken)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
