# Mobile Architecture Rules

KMP + Compose Multiplatform architecture rules. Mobile implementation has not yet started but these rules define the target architecture.

## Files

| File | Description |
|------|-------------|
| `clean-architecture.md` | KMP clean architecture: shared domain in `commonMain`, platform-specific in `androidMain`/`iosMain`. Same dependency rules as API — domain has no platform dependencies |
| `components.md` | Compose Multiplatform component conventions: naming, state hoisting, preview annotations, accessibility requirements |
| `di.md` | Koin DI setup for KMP — module definitions, `startKoin` in platform entry points, expect/actual for platform-specific bindings |
| `navigation.md` | Navigation Compose setup: `NavHost`, route definitions, deep link handling, back stack management |
| `styling.md` | Material 3 theming: `MaterialTheme`, color scheme, typography scale, shape system, dark mode support |
| `testing.md` | KMP test strategy: `commonTest` for domain logic, `androidTest` with Compose test rules, `iosTest` with XCTest bridge |
