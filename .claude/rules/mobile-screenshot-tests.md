---
paths:
  - "mobile/**/*ScreenshotTest*"
  - "mobile/**/*Screenshot*"
  - "mobile/**/roborazzi/**"
  - "mobile/**/screenshots/**"
---

# Screenshot Testing — Mobile

## Framework: Roborazzi

JVM-based screenshot testing for Compose. Uses Robolectric to render composables — no emulator needed. Captures PNG screenshots and compares pixel-by-pixel against reference images.

## Test Pattern

One test file per screen, one test method per UiState variant:

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DashboardScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test fun dashboard_idle() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardIdle)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Idle.png")
    }
}
```

## Rules

- Test **stateless** `*Content` composables via `*ForTest` wrapper functions (never Screen classes with ViewModel injection)
- Use `TestApplication` (no Koin) — screenshot tests render pure UI only
- Use `PreviewData` for consistent test data across all screenshots
- Naming: `ScreenName_State.png` (e.g., `Dashboard_Idle.png`, `Login_Error.png`)
- Screenshots stored at `mobile/androidApp/src/test/screenshots/`

## Gradle Commands

```bash
# Record new reference screenshots (after visual changes)
./gradlew :androidApp:recordRoborazziDebug

# Verify screenshots match references (CI gate)
./gradlew :androidApp:verifyRoborazziDebug

# Compare and generate diff images
./gradlew :androidApp:compareRoborazziDebug
```

## When to Update

- After ANY theme change (Color.kt, Shape.kt, Type.kt)
- After ANY screen layout change
- After adding new UiState variants
- Run `recordRoborazziDebug` to update references, then commit the new PNGs

Full reference: `rules/architecture-mobile/testing.md`
