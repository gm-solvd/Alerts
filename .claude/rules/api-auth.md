---
paths:
  - "api/**/auth/**"
  - "api/**/config/Security*"
  - "api/**/config/Jwt*"
  - "api/**/JwtAuth*"
---

# Authentication — API

## Dual-Mode Auth

- Email + password (BCrypt hash, strength 12)
- OAuth2 (Google/Apple) — mobile gets idToken, POST to `/auth/oauth2/callback`, backend verifies, issues app JWT

## JWT Configuration

- Algorithm: HS256
- Access tokens: 15-minute expiry
- Refresh tokens: 30-day expiry with rotation on use
- `authenticatedUserId()` extracts UUID from SecurityContext

## Endpoints

- `POST /auth/register` — email + password registration
- `POST /auth/login` — email + password login
- `POST /auth/oauth2/callback` — OAuth2 token exchange
- `POST /auth/refresh` — rotate refresh token, issue new access token

## Security Rules

- Rotate refresh tokens on every use
- Invalidate all tokens on password change
- Rate-limit auth endpoints
- Log auth events (login, register, token refresh, failed attempts)
- Never store plain-text passwords

Full reference: `rules/architecture-api/auth.md`
