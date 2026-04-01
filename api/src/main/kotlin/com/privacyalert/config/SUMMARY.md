# Config Layer

Spring `@Configuration` and filter classes. Wires up security, JWT, and external configuration. The only layer that directly imports Spring Security / framework infrastructure.

## Files

| File | Description |
|------|-------------|
| `SecurityConfig.kt` | Spring Security configuration: disables CSRF (stateless API), sets `SessionCreationPolicy.STATELESS`, configures CORS, registers `JwtAuthFilter` and `AdminTokenFilter`, defines public routes (`/api/v1/auth/**`) and admin-only routes (`/api/v1/admin/**`) |
| `JwtAuthFilter.kt` | `OncePerRequestFilter` — extracts `Bearer` token from `Authorization` header, validates via `JwtProvider`, sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder` |
| `JwtProviderImpl.kt` | Implements `JwtProvider` using JJWT library. Generates HS256 JWTs: access tokens (15 min expiry), refresh tokens (30 day expiry). Validates signature and expiry on parse |
| `AdminTokenFilter.kt` | Separate filter for admin endpoints. Validates a static admin bearer token (configured via `app.admin.token`) before allowing access to `/api/v1/admin/**` |
| `AppProperties.kt` | `@ConfigurationProperties(prefix = "app")` binding for: `hibp.enabled`, `hibp.api-key`, `jwt.secret`, `jwt.access-expiry`, `jwt.refresh-expiry`, `admin.token` |
| `BcryptPasswordEncoderAdapter.kt` | Adapts Spring Security's `BCryptPasswordEncoder` to the domain `PasswordEncoder` interface, keeping Spring out of the domain layer |
