# Data Repositories

Two layers: Spring Data JPA interfaces (`*JpaRepository`) and adapter classes (`*RepositoryAdapter`) that implement the domain repository interfaces.

## JPA Repository Interfaces

| File | Extends |
|------|---------|
| `UserJpaRepository.kt` | `JpaRepository<UserEntity, UUID>` + custom `findByEmail`, `findByOAuthProviderAndOAuthSub` |
| `AlertJpaRepository.kt` | `JpaRepository<AlertEntity, UUID>` + `findByUserId`, filtered queries |
| `ScanResultJpaRepository.kt` | `JpaRepository<ScanResultEntity, UUID>` + `findByUserId` |
| `ScoreHistoryJpaRepository.kt` | `JpaRepository<ScoreHistoryEntity, UUID>` + `findTopByUserIdOrderByTimestampDesc`, paginated history |
| `RefreshTokenJpaRepository.kt` | `JpaRepository<RefreshTokenEntity, UUID>` + `findByTokenHash`, `revokeAllByUserId` |
| `MitigationJpaRepository.kt` | `JpaRepository<MitigationEntity, UUID>` + `findByAlertId`, `findByUserId` |
| `PasteFindingJpaRepository.kt` | `JpaRepository<PasteFindingEntity, UUID>` |
| `KnownBreachJpaRepository.kt` | `JpaRepository<KnownBreachEntity, UUID>` + `findByDomain` |
| `DataBrokerSiteJpaRepository.kt` | `JpaRepository<DataBrokerSiteEntity, UUID>` |
| `BreachDatabaseJpaRepository.kt` | `JpaRepository<BreachedCredentialEntity, UUID>` + `findByEmailIgnoreCase` |

## Repository Adapters

Each adapter implements the corresponding domain interface, delegates to the JPA repo, and converts between entities and domain models.

| File | Implements |
|------|-----------|
| `UserRepositoryAdapter.kt` | `UserRepository` |
| `AlertRepositoryAdapter.kt` | `AlertRepository` |
| `ScanResultRepositoryAdapter.kt` | `ScanResultRepository` |
| `ScoreRepositoryAdapter.kt` | `ScoreRepository` |
| `RefreshTokenRepositoryAdapter.kt` | `RefreshTokenRepository` |
| `MitigationRepositoryAdapter.kt` | `MitigationRepository` |
| `PasteFindingRepositoryAdapter.kt` | `PasteFindingRepository` |
| `KnownBreachRepositoryAdapter.kt` | `BreachDatabaseRepository` |
| `DataBrokerSiteRepositoryAdapter.kt` | `DataBrokerSiteRepository` |
