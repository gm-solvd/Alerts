# Dependency Injection — Mobile (Koin)

## Setup
Koin is initialized in the shared module and platform entry points.

---

## Module Organization

```kotlin
// shared/di/AppModule.kt
val domainModule = module {
    factory { GetAlertsUseCase(get()) }
    factory { GetExposureScoreUseCase(get()) }
    factory { ScanBreachUseCase(get()) }
    factory { ResolveAlertUseCase(get(), get()) }
    factory { GetMitigationsUseCase(get()) }
    factory { AuditPermissionsUseCase(get()) }
}

val dataModule = module {
    single { ApiClient.create() }           // Ktor HttpClient — singleton
    single<AlertRepository> { AlertRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<ScanRepository> { ScanRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { AlertsViewModel(get(), get()) }
    viewModel { ScanViewModel(get()) }
    viewModel { MitigationsViewModel(get()) }
}

val appModule = listOf(domainModule, dataModule, viewModelModule)
```

---

## Platform Entry Points

```kotlin
// androidApp — Application.kt
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}

// iosApp — iOSApp.swift (via KMP helper)
fun initKoin() {
    startKoin { modules(appModule) }
}
```

---

## Rules

- Use `single` for stateful/expensive objects (HTTP clients, DB drivers, repositories)
- Use `factory` for use cases — they're stateless, a new instance per injection is fine
- Use `viewModel` for ViewModels — Koin manages their lifecycle
- Never call `get()` (Koin service locator) inside business logic — inject via constructor
- Platform-specific dependencies (e.g., SQLDelight driver) are provided via `expect/actual`

---

## expect/actual for Platform Deps

```kotlin
// shared — expect
expect fun createSqlDriver(dbName: String): SqlDriver

// androidApp — actual
actual fun createSqlDriver(dbName: String): SqlDriver =
    AndroidSqliteDriver(PrivacyAlertDb.Schema, context, dbName)

// iosApp — actual
actual fun createSqlDriver(dbName: String): SqlDriver =
    NativeSqliteDriver(PrivacyAlertDb.Schema, dbName)
```

Register in `dataModule`:
```kotlin
single { createSqlDriver("privacy_alert.db") }
single { PrivacyAlertDb(get()) }
```
