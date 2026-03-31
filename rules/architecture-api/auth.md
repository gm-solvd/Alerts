# Authentication — API

## Strategy
Dual-mode authentication:
1. **Email + password** → issue JWT on login/register
2. **Social login (Google / Apple)** → OAuth2/OIDC → issue JWT after callback

All protected endpoints require a valid JWT in the `Authorization` header.

---

## JWT

- Algorithm: `HS256` (or `RS256` for production hardening)
- Access token TTL: **15 minutes**
- Refresh token TTL: **30 days** (stored in DB, rotated on use)
- Header: `Authorization: Bearer <token>`

### Payload Claims
```json
{
  "sub": "<user-id UUID>",
  "email": "user@example.com",
  "iat": 1234567890,
  "exp": 1234568790
}
```

---

## Endpoints

```
POST /api/v1/auth/register
  Body: { email, password }
  Returns: { accessToken, refreshToken }

POST /api/v1/auth/login
  Body: { email, password }
  Returns: { accessToken, refreshToken }

POST /api/v1/auth/oauth2/callback
  Body: { provider: "google" | "apple", idToken }
  Returns: { accessToken, refreshToken }

POST /api/v1/auth/refresh
  Body: { refreshToken }
  Returns: { accessToken, refreshToken }
```

---

## Password Storage
- Hash with **BCrypt** (`BCryptPasswordEncoder`, strength 12)
- Never store plain text or reversible encryption
- OAuth-only users have `null` password hash

---

## Spring Security Config

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtFilter: JwtAuthFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
```

---

## OAuth2 Flow (Google / Apple)

1. Mobile obtains `idToken` from Google/Apple SDK
2. Mobile sends `idToken` to `POST /api/v1/auth/oauth2/callback`
3. Backend verifies `idToken` using Google/Apple public keys
4. Look up or create user by `oauth_subject` + `oauth_provider`
5. Issue app JWT — same flow as email/password from here

> Apple Sign-In on iOS requires `com.apple.developer.applesignin` entitlement.

---

## Security Rules
- Rotate refresh tokens on every use (one-time tokens)
- Invalidate all refresh tokens on password change
- Rate-limit `/auth/login` and `/auth/register` (e.g., 5 req/min per IP)
- Never return password hashes in any response
- Log auth events (login, register, token refresh, failure) for audit trail
