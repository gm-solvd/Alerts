# Data Entities

JPA `@Entity` classes. Each entity mirrors a domain model and provides two mapper methods: `toDomain()` and a companion `toEntity(domainModel)`. Never exposed outside the `data/` layer.

## Files

| File | Table | Notes |
|------|-------|-------|
| `UserEntity.kt` | `users` | Maps `User` domain model; stores password hash, OAuth fields |
| `AlertEntity.kt` | `alerts` | Maps `Alert`; stores `ThreatCategory` and `Severity` as strings |
| `ScanResultEntity.kt` | `scan_results` | `findings` stored as TEXT (not JSONB) to avoid serialization issues |
| `ScoreHistoryEntity.kt` | `score_history` | Append-only score snapshots per user |
| `RefreshTokenEntity.kt` | `refresh_tokens` | Stores hashed token, expiry, revoked flag |
| `MitigationEntity.kt` | `mitigations` | Alert remediation steps with completion state |
| `PasteFindingEntity.kt` | `paste_findings` | Paste site exposure records |
| `KnownBreachEntity.kt` | `known_breaches` | Local breach database seeded via Flyway |
| `DataBrokerSiteEntity.kt` | `data_broker_sites` | Data broker sites where user PII was found |
| `BreachedCredentialEntity.kt` | `breached_credentials` | Individual credentials from known breach sets |

## Convention

All entities implement `Persistable<UUID>` to ensure Spring Data does an `INSERT` (not `SELECT` + `UPDATE`) for new records with pre-assigned UUIDs.
