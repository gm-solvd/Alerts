# Commit Conventions

## Format
```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

## Types

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change that is not a fix or feature |
| `test` | Adding or updating tests |
| `docs` | Documentation only |
| `chore` | Build, tooling, dependency updates |
| `style` | Formatting, no logic change |
| `perf` | Performance improvement |

## Scopes

| Scope | Area |
|---|---|
| `mobile` | KMP shared or cross-platform code |
| `android` | Android-specific |
| `ios` | iOS-specific |
| `api` | Backend / Spring Boot |
| `db` | Database schema or migrations |
| `auth` | Authentication |
| `alerts` | Alert feature |
| `score` | Exposure score |
| `scan` | Breach/identity scan |

## Examples
```
feat(alerts): add severity filter to alerts feed
fix(api): handle null response from HIBP on unknown email
refactor(mobile): extract score computation to dedicated use case
test(api): add integration test for breach scan endpoint
chore(db): add index on alerts.user_id
```

## Rules
- Subject line: imperative mood, max 72 chars, no period at end
- Body: explain *why*, not *what* (the diff shows what)
- Breaking changes: add `BREAKING CHANGE:` footer
- Reference issues: `Closes #123` in footer
