# Testing — Mobile (KMP)

## Test Pyramid

```
        [UI Tests]          ← Compose UI tests, happy-path flows
      [ViewModel Tests]     ← UiState transitions, coroutine testing
    [Use Case Tests]        ← Business logic, mock repositories
  [Repository Tests]        ← Ktor MockEngine, SQLDelight in-memory
```

---

## Use Case Tests

Pure Kotlin — no Android framework needed, runs on JVM.

```kotlin
class ScanBreachUseCaseTest {
    private val scanRepository = mockk<ScanRepository>()
    private val useCase = ScanBreachUseCase(scanRepository)

    @Test
    fun `returns breach list when email is found in database`() = runTest {
        val email = "test@example.com"
        val breaches = listOf(Breach(service = "Adobe", date = "2013-10-04"))
        coEvery { scanRepository.scanBreach(email) } returns breaches

        val result = useCase(email)

        assertTrue(result.isSuccess)
        assertEquals(breaches, result.getOrNull())
    }

    @Test
    fun `returns failure when network error occurs`() = runTest {
        coEvery { scanRepository.scanBreach(any()) } throws IOException("timeout")

        val result = useCase("test@example.com")

        assertTrue(result.isFailure)
        assertIs<AppError.NetworkError>(result.exceptionOrNull())
    }
}
```

---

## ViewModel Tests

Use `kotlinx-coroutines-test` and `Turbine` for Flow assertions.

```kotlin
class DashboardViewModelTest {
    @get:Rule val coroutineRule = MainCoroutineRule()

    private val getScore = mockk<GetExposureScoreUseCase>()
    private val getAlerts = mockk<GetAlertsUseCase>()

    @Test
    fun `emits Success state after loading`() = runTest {
        val score = ExposureScore(value = 72)
        val alerts = emptyList<Alert>()
        coEvery { getScore() } returns Result.success(score)
        coEvery { getAlerts(any()) } returns Result.success(alerts)

        val vm = DashboardViewModel(getScore, getAlerts)

        vm.uiState.test {
            assertIs<DashboardUiState.Loading>(awaitItem())
            assertIs<DashboardUiState.Success>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## Repository Tests

Test remote data sources using **Ktor MockEngine**:

```kotlin
@Test
fun `scanBreach returns parsed breach list`() = runTest {
    val engine = MockEngine { request ->
        respond(
            content = """[{"Name":"Adobe","BreachDate":"2013-10-04"}]""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    val repo = ScanRepositoryImpl(client)

    val result = repo.scanBreach("test@example.com")

    assertEquals("Adobe", result.first().service)
}
```

---

## UI Tests (Compose)

```kotlin
@Test
fun dashboardShowsScoreWhenLoaded() {
    val fakeVm = FakeDashboardViewModel(
        state = DashboardUiState.Success(ExposureScore(72), emptyList())
    )
    composeTestRule.setContent { DashboardScreen(viewModel = fakeVm) }

    composeTestRule.onNodeWithText("72").assertIsDisplayed()
}
```

---

## Naming Convention
```
`<method/action> <condition> <expected outcome>`

returns breach list when email is found in database
emits Success state after loading
returns failure when network error occurs
```

## Tools
| Purpose | Library |
|---|---|
| Mocking | MockK |
| Flow testing | Turbine |
| Coroutine testing | kotlinx-coroutines-test |
| HTTP mocking | Ktor MockEngine |
| UI testing | Compose UI Test |
