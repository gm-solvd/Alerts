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

## Dependent / Stacked PRs

When a feature is split into phases (e.g., backend then frontend), use **stacked PRs**:

| PR | Base Branch | Example |
|---|---|---|
| Phase 1 (backend) | `develop` | `feat/scan-async-backend` → `develop` |
| Phase 2 (frontend) | Phase 1's branch | `feat/scan-async-frontend` → `feat/scan-async-backend` |

### Creating a dependent PR

1. Create Phase 2 branch **from Phase 1's branch** (not from develop)
2. Set `gh pr create --base <phase-1-branch>`
3. Add a **Dependencies** section in the PR body:
   ```markdown
   ## Dependencies
   - Depends on #<PR-number> — must be merged first
   ```

### Merging stacked PRs

1. Merge Phase 1 into `develop` (squash merge)
2. Rebase Phase 2 onto `develop`: `git rebase develop`
3. Change Phase 2 base to `develop`: `gh pr edit <number> --base develop`
4. Merge Phase 2 into `develop`

## Review Checklist
- [ ] Code follows clean architecture layer rules
- [ ] No business logic in Composables or Controllers
- [ ] New public functions have clear naming (no abbreviations)
- [ ] No hardcoded strings (use resources/constants)
- [ ] Error cases handled
- [ ] Tests cover the happy path and at least one failure case
