# Scraping Infrastructure

HTTP client utilities shared by all integration scanners that perform web scraping.

## Files

| File | Description |
|------|-------------|
| `RateLimitedHttpClient.kt` | Wraps Spring's `RestTemplate`/`WebClient` with configurable rate limiting (requests per second per domain) and exponential backoff on 429/503 responses |
| `RobotsTxtChecker.kt` | Fetches and caches `robots.txt` for target domains; checks whether the scanner's user-agent is permitted to access a given path before making requests |

## Usage

All integration clients that scrape external sites must use `RateLimitedHttpClient` and check `RobotsTxtChecker` before accessing any URL. This ensures compliance with rate limits and web crawling etiquette.
