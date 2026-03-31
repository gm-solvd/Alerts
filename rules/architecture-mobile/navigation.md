# Navigation — Mobile (Voyager)

## Library
**Voyager** — multiplatform-first navigation for Compose Multiplatform.

---

## Screen Definition

```kotlin
// Each screen is a Voyager Screen object
class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<DashboardViewModel>()
        DashboardContent(
            uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
            onAlertClick = { id -> navigator.push(AlertDetailScreen(id)) },
            onScanClick = { navigator.push(ScanScreen()) }
        )
    }
}

data class AlertDetailScreen(val alertId: UUID) : Screen {
    @Composable
    override fun Content() { ... }
}
```

---

## Navigation Graph

```
AuthFlow
  ├── LoginScreen
  └── RegisterScreen

MainFlow (after auth)
  ├── DashboardScreen          ← default tab
  ├── AlertsScreen
  │   └── AlertDetailScreen    ← push on tap
  ├── ScanScreen
  │   ├── BreachScanScreen     ← breach scan via HIBP
  │   ├── IdentityScanScreen   ← social media footprint search
  │   ├── PermissionAuditScreen ← device permission audit (synced to API)
  │   └── ScanResultScreen     ← push after any scan
  └── MitigationsScreen
```

---

## Bottom Navigation

```kotlin
enum class Tab(val screen: Screen, val icon: ImageVector, val label: String) {
    DASHBOARD(DashboardScreen(), Icons.Default.Home, "Dashboard"),
    ALERTS(AlertsScreen(), Icons.Default.Notifications, "Alerts"),
    SCAN(ScanScreen(), Icons.Default.Search, "Scan"),
    FIX_IT(MitigationsScreen(), Icons.Default.Build, "Fix It")
}

@Composable
fun MainScaffold() {
    val tabNavigator = rememberTabNavigator()
    TabNavigator(Tab.DASHBOARD) {
        Scaffold(
            bottomBar = { BottomNavBar(Tab.entries) }
        ) { AppContent() }
    }
}
```

---

## Passing Arguments
Use `data class` Screen with constructor params — Voyager serializes them automatically with `@Parcelize` on Android:

```kotlin
@Parcelize
data class AlertDetailScreen(val alertId: UUID) : Screen, Parcelable {
    @Composable
    override fun Content() { ... }
}
```

---

## Auth Guard
Check auth state at app startup — navigate to `AuthFlow` if no valid token:

```kotlin
@Composable
fun AppEntry(authViewModel: AuthViewModel = koinViewModel()) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    if (isAuthenticated) {
        Navigator(DashboardScreen())
    } else {
        Navigator(LoginScreen())
    }
}
```

---

## Rules
- Never pass full domain models as navigation arguments — pass IDs only, load in destination ViewModel
- Deep links map to Screen constructors
- Back stack is managed by Voyager — don't manually pop unless implementing custom back behavior
- Each Screen's `Content()` is the only place to inject a ViewModel (via `koinScreenModel()`)
