# Integration Layer

External API clients and web scraping utilities. Implements domain scanner interfaces. All external calls are rate-limited and robots.txt compliant.

## Files

| File | Interface Implemented | Description |
|------|----------------------|-------------|
| `CompositeBreachScanner.kt` | `BreachScanner` (`@Primary`) | Merges results from `LocalBreachScannerImpl` + `PasteMonitorClient` + optional HIBP. Deduplicates by breach name |
| `LocalBreachScannerImpl.kt` | `BreachScanner` | Queries the local `KnownBreach` database (seeded via Flyway) for email/phone matches |
| `HibpClientImpl.kt` | — | Optional Have I Been Pwned v3 API client. Only active when `app.hibp.enabled=true`. Uses k-Anonymity model for pwned password checks |
| `PasteMonitorClient.kt` | — | Monitors paste sites (Pastebin, etc.) for email/phone exposure via `RateLimitedHttpClient` |
| `IdentityExposureScannerImpl.kt` | `IdentityExposureScanner` | Detects user profiles on public identity aggregator sites |
| `PiiExposureScannerImpl.kt` | `PiiExposureScanner` | Searches public people-finder databases and trackers for name/phone/address exposure |
| `SocialFootprintScannerImpl.kt` | `SocialFootprintScanner` | Discovers social media profiles (by username/email) across major platforms |
| `OAuthVerifierImpl.kt` | `OAuthVerifier` | Verifies Google/Apple OAuth ID tokens via their public key endpoints |

## Sub-directories

| Directory | Purpose |
|-----------|---------|
| [`scraping/`](scraping/SUMMARY.md) | HTTP infrastructure: `RateLimitedHttpClient` (rate limiting + exponential backoff), `RobotsTxtChecker` (respects robots.txt rules before scraping) |

## Configuration

- HIBP: disabled by default, enable via `app.hibp.enabled=true` and `app.hibp.api-key=<key>`
- All HTTP clients use `RateLimitedHttpClient` to avoid overwhelming external services
