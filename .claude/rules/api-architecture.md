---
paths:
  - "api/**/*.kt"
---

# API Architecture

Clean Architecture with 4 layers. Dependency rule: each layer depends only on layers below.

```
api (controllers) → domain (services) → data (persistence) → integration (external)
```

## Package Structure

```
com.privacyalert/
├── domain/
│   ├── model/          # Pure Kotlin data classes — no JPA annotations
│   ├── service/        # Business logic — injected via interfaces
│   └── repository/     # Interfaces only — implemented in data/
├── data/
│   ├── entity/         # JPA @Entity with toEntity()/toDomain() mappers
│   └── repository/     # JpaRepository + RepositoryAdapter implementations
├── api/
│   ├── controller/     # Thin @RestController — parse input, call service, return DTO
│   └── dto/            # Request/Response objects (never expose domain models)
├── integration/        # External clients (HIBP, breach scanners, web scrapers)
└── config/             # Spring @Configuration, security, JWT, properties
```

## Layer Rules

- **Domain**: Zero Spring/JPA dependencies. Services receive/return domain models only.
- **API**: Thin controllers — no business logic. DTOs stay here, never leak to domain.
- **Data**: JPA entities with `toEntity()`/`toDomain()` mappers. Entities never leave this layer.
- **Integration**: External HTTP clients. Errors wrapped as domain exceptions.

## DI Rules

- Constructor injection exclusively — no field injection.
- `@Service` for domain services, `@RestController` for controllers, `@Repository` for data.
- Inject interfaces, not concrete implementations.
- External clients configured via `@Configuration` + `@Bean`.

## REST Conventions

- Base URL: `/api/v1/`
- Endpoints: GET (list/get), POST (create), PATCH (partial update), DELETE
- Separate Request/Response DTOs. Validation on Request only.
- Pagination: `PageResponse<T>` with content, page, size, totalElements, totalPages.
- Status codes: 200/201/204/400/401/403/404/502/500.

Full reference: `rules/architecture-api/`
