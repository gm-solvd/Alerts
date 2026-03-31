# Clean Architecture — API

## Layer Overview

```
┌─────────────────────────────┐
│        api (controllers)    │  ← Spring Web, DTOs, HTTP concerns
├─────────────────────────────┤
│       domain (services)     │  ← Business logic, pure Kotlin, no frameworks
├─────────────────────────────┤
│       data (persistence)    │  ← JPA entities, Spring Data repos
├─────────────────────────────┤
│     integration (external)  │  ← HIBP, data broker clients
└─────────────────────────────┘
```

**Dependency rule**: each layer depends only on layers below it. Domain has **zero** Spring/JPA dependencies.

---

## Package Structure

```
com.privacyalert/
├── domain/
│   ├── model/          # Pure Kotlin data classes — no JPA annotations
│   ├── service/        # Business logic — injected via interfaces
│   └── repository/     # Interfaces only — implemented in data/
├── data/
│   ├── entity/         # JPA @Entity classes
│   └── repository/     # Spring Data JPA interfaces + impl
├── api/
│   ├── controller/     # @RestController — thin, delegates to services
│   └── dto/            # Request/Response objects — no domain models exposed
├── integration/        # External API clients
└── config/             # Spring config beans
```

---

## Rules

### Domain layer
- No `@Component`, `@Service`, `@Repository`, or any Spring annotations
- No JPA annotations (`@Entity`, `@Column`, etc.)
- No DTOs — only domain models
- Services receive and return domain models, not entities or DTOs

### API layer
- Controllers are **thin** — they parse input, call one service, return a response
- Always map to/from DTOs at this layer; never expose domain or entity classes directly
- Validation annotations (`@Valid`, `@NotBlank`) belong on DTOs

### Data layer
- JPA entities exist only here
- Repository interfaces extend `JpaRepository`
- Entities map to/from domain models via mapper functions (no MapStruct for now — plain `toEntity()` / `toDomain()` extension functions)

---

## Example Flow

```
POST /api/alerts/resolve/{id}
  → AlertController.resolve(id)
    → AlertService.resolveAlert(id)          // domain
      → AlertRepository.findById(id)         // domain interface
        → AlertJpaRepository.findById(id)    // data impl
      → alert.copy(resolved = true)
      → AlertRepository.save(alert)
    → ResponseEntity.ok(alert.toDto())
```
