package com.privacyalert.config

import com.privacyalert.domain.service.JwtProvider
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtProviderImpl(
    private val appProperties: AppProperties,
) : JwtProvider {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(appProperties.jwt.secret.toByteArray())
    }

    override fun generateAccessToken(
        userId: UUID,
        email: String,
    ): String {
        val now = Date()
        val expiry = Date(now.time + appProperties.jwt.accessTokenTtl.toMillis())

        return Jwts
            .builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    override fun generateRefreshToken(): String = UUID.randomUUID().toString()

    override fun validateAndExtractUserId(token: String): UUID? =
        try {
            val claims =
                Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            UUID.fromString(claims.subject)
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
}
