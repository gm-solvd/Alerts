# DTOs (Data Transfer Objects)

Request and response objects for the HTTP API. DTOs are never domain models — they exist only in the `api/` layer and are mapped to/from domain objects in controllers.

## Files

| File | Contents |
|------|---------|
| `AlertDtos.kt` | `AlertResponse` (all alert fields for API output), `AlertListResponse` (paginated wrapper) |
| `AuthDtos.kt` | `RegisterRequest` (email, password, fullName), `LoginRequest` (email, password), `OAuthCallbackRequest` (provider, idToken), `RefreshTokenRequest` (refreshToken), `AuthTokensResponse` (accessToken, refreshToken) |
| `ScanDtos.kt` | `ScanProfileRequest` (override fields for scanning), `ScanResultResponse` (scanType, findingsCount, timestamp) |
| `ScoreDtos.kt` | `ScoreResponse` (score, calculatedAt), `ScoreHistoryResponse` (paginated list of score records) |
| `MitigationDtos.kt` | `MitigationResponse`, `CompleteMitigationResponse` |
| `PermissionAuditDtos.kt` | `PermissionAuditRequest` (permissions: List<String>), `PermissionAuditResponse` (alertsCreated count) |
| `AdminDtos.kt` | `CreateUserRequest`, `AdminUserResponse` (user summary + alert stats), `AdminUserDetailResponse` (full user with recent alerts), `AdminStatsResponse` (platform totals + breakdowns), `ScanExecutionResponse` |
| `PageResponse.kt` | Generic `PageResponse<T>` (content, page, size, totalElements, totalPages) |
| `ErrorResponse.kt` | `ErrorResponse` (status, message, timestamp) returned by `GlobalExceptionHandler` |
