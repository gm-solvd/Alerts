# Domain Models

Pure Kotlin data classes — no Spring, no JPA. These are the core business objects passed between all layers.

## Files

| File | Description |
|------|-------------|
| `Alert.kt` | Privacy threat alert — UUID, userId, category (`ThreatCategory`), severity (`Severity`), title, description, resolved flag, timestamps |
| `AppException.kt` | Domain exception hierarchy: `ResourceNotFoundException` (404), `ConflictException` (409), `UnauthorizedException` (401), `ForbiddenException` (403), `BadRequestException` (400) |
| `AuthTokens.kt` | Response model pairing an access token and a refresh token returned after successful authentication |
| `DataBrokerSite.kt` | Represents a data broker site where user PII was found (name, URL, found data types) |
| `KnownBreach.kt` | Entry from the breach database (breach name, date, affected domain, exposed data classes) |
| `Mitigation.kt` | Actionable remediation step for an alert (alertId, description, completed flag, timestamps) |
| `PasteFinding.kt` | Exposure record on a paste site (site name, URL, breach date, user email/phone found) |
| `RefreshToken.kt` | JWT refresh token record (userId, tokenHash, expiresAt, revoked flag) |
| `ScanResult.kt` | Record of a completed scan execution (userId, scanType enum, findings as string, timestamp) |
| `ScoreRecord.kt` | Privacy health score snapshot (userId, score 0–100, timestamp) |
| `Severity.kt` | Enum defining penalty points: `CRITICAL=15`, `HIGH=10`, `MEDIUM=5`, `LOW=2` |
| `ThreatCategory.kt` | Enum defining category weights used in scoring: `DATA_BREACH=1.0`, `IDENTITY_EXPOSURE=0.9`, `PII_EXPOSURE=0.8`, `SOCIAL_FOOTPRINT=0.6`, `APP_OVERPERMISSIONS=0.5` |
| `User.kt` | User identity (UUID, email, passwordHash, fullName, phone, address, DOB, OAuth provider, roles) |
| `UserScanProfile.kt` | Aggregated profile used as input to all scanners (email, phone, fullName, address, DOB) |

## Key Relationships

```
User ──< Alert ──< Mitigation
User ──< ScoreRecord
User ──< ScanResult
User ──< RefreshToken
Alert references ThreatCategory and Severity
ScanService uses UserScanProfile as scanner input
```
