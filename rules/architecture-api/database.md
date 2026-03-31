# Database Conventions — API

## Technology
PostgreSQL via Spring Data JPA + Hibernate.

---

## Schema

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),                       -- null for OAuth-only
    oauth_provider  VARCHAR(50),                        -- 'google' | 'apple'
    oauth_subject   VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category        VARCHAR(50) NOT NULL,
    severity        VARCHAR(20) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    resolved        BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_alerts_user_id ON alerts(user_id);
CREATE INDEX idx_alerts_severity ON alerts(severity);

CREATE TABLE score_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    score           SMALLINT NOT NULL,
    recorded_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_score_history_user_id ON score_history(user_id);
```

---

## Entity Conventions

- Use `UUID` as primary key — annotate with `@GeneratedValue(strategy = GenerationType.UUID)`
- Use `Instant` for timestamps, mapped to `TIMESTAMP WITH TIME ZONE`
- No `@Column(name = ...)` unless the DB column name differs from the field name
- Entities are in `data/entity/` — never exposed outside the data layer

```kotlin
@Entity
@Table(name = "alerts")
class AlertEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    val category: ThreatCategory,

    @Enumerated(EnumType.STRING)
    val severity: Severity,

    val title: String,
    val description: String,
    var resolved: Boolean = false,
    var resolvedAt: Instant? = null,
    val createdAt: Instant = Instant.now()
)
```

---

## Migrations
- Use **Flyway** for schema migrations
- Files in `src/main/resources/db/migration/`
- Naming: `V<version>__<description>.sql` (e.g., `V1__create_users_table.sql`)
- Never modify an existing migration — always add a new one
- Migrations run automatically on app startup

---

## Repository Conventions

```kotlin
// domain/repository — interface only
interface AlertRepository {
    fun findById(id: UUID): Alert?
    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Alert>
    fun save(alert: Alert): Alert
    fun delete(id: UUID)
}

// data/repository — Spring Data implementation
@Repository
interface AlertJpaRepository : JpaRepository<AlertEntity, UUID> {
    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<AlertEntity>
}
```

Map between entity and domain model with extension functions:
```kotlin
fun AlertEntity.toDomain(): Alert = Alert(id, userId, category, severity, ...)
fun Alert.toEntity(): AlertEntity = AlertEntity(id, userId, category, severity, ...)
```
