# Pull Request Guidelines

## PR Title
Follow the same format as commits: `<type>(<scope>): <short description>`

## PR Description Template
```markdown
## What
Brief description of the change.

## Why
The motivation or problem being solved.

## How
Key implementation decisions, if non-obvious.

## Test plan
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manually tested on Android
- [ ] Manually tested on iOS (if UI change)

## Screenshots (if UI change)
| Before | After |
|---|---|
| | |
```

## Rules
- **One concern per PR** — avoid mixing unrelated changes
- **Draft PRs** for work-in-progress; convert to Ready when complete
- **Self-review** before requesting review — read your own diff first
- **No force-push** to shared branches (main, develop)
- **Squash merge** preferred to keep history clean

## Branch Naming
```
<type>/<scope>-<short-description>
feat/alerts-severity-filter
fix/api-hibp-null-response
refactor/mobile-score-usecase
```

## Review Checklist
- [ ] Code follows clean architecture layer rules
- [ ] No business logic in Composables or Controllers
- [ ] New public functions have clear naming (no abbreviations)
- [ ] No hardcoded strings (use resources/constants)
- [ ] Error cases handled
- [ ] Tests cover the happy path and at least one failure case
