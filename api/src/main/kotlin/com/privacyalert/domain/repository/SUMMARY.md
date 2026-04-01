# Domain Repository Interfaces

Pure Kotlin interfaces — no Spring, no JPA. These define the persistence contract for the domain layer. All implementations live in the `data/` layer.

## Files

| Interface | Key Operations |
|-----------|---------------|
| `AlertRepository` | `findByUserId(userId, filter)`, `findById(id)`, `save(alert)`, `delete(id)` |
| `BreachDatabaseRepository` | `findByEmail(email)`, `findByDomain(domain)`, `findAll()` |
| `DataBrokerSiteRepository` | `findByUserId(userId)`, `save(site)`, `deleteByUserId(userId)` |
| `MitigationRepository` | `findByAlertId(alertId)`, `findByUserId(userId)`, `save(mitigation)` |
| `PasteFindingRepository` | `findByUserId(userId)`, `findByEmailOrPhone(email, phone)`, `save(finding)` |
| `RefreshTokenRepository` | `findByTokenHash(hash)`, `save(token)`, `revokeByUserId(userId)` |
| `ScanResultRepository` | `findByUserId(userId)`, `findById(id)`, `save(result)` |
| `ScoreRepository` | `findLatestByUserId(userId)`, `findHistoryByUserId(userId, pageable)`, `save(record)` |
| `UserRepository` | `findById(id)`, `findByEmail(email)`, `findByOAuthSub(provider, sub)`, `save(user)`, `delete(id)`, `findAll(pageable)` |

## Design Pattern

Each interface uses domain models only — no `Optional`, no `Page` (Spring types). Adapters in `data/repository/` translate between JPA return types and domain types.
