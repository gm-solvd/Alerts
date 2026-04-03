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
- **Never push directly to `develop` or `main`** — all changes go through a PR. Exception: only when explicitly instructed by the user.

## Terminology

- **Feature** — the top-level implementation goal (e.g., "Async scan + structured findings")
- **Task** — a workstream PR within a feature (e.g., "Web Backend task", "Web Frontend task")

## Branch Naming
```
<type>/<scope>-<short-description>
feat/alerts-severity-filter
fix/api-hibp-null-response
refactor/mobile-score-usecase
```

## Parent / Child Branch Pattern

When a feature has multiple related tasks, use a **parent/child** branch pattern:

```
feat/parent-feature-name          ← branches off develop
├── feat/child-task-1             ← branches off parent, PR targets parent
├── feat/child-task-2             ← branches off parent, PR targets parent
└── feat/child-task-n             ← branches off parent, PR targets parent
```

| Branch | Base | Example |
|---|---|---|
| Parent | `develop` | `feat/mobile-phase1-foundation` → `develop` |
| Child 1 | Parent | `feat/mobile-network-layer` → `feat/mobile-phase1-foundation` |
| Child 2 | Parent | `feat/mobile-viewmodels` → `feat/mobile-phase1-foundation` |

### Rules

- **Parent** branches off `develop`
- **Children** branch off the parent and merge back into the parent via PR
- After **all children** are merged, the **parent** merges into `develop` via PR
- Branch names must be **relevant** — clearly describe what the branch does
- Each child PR must be independently reviewable with a clear scope

### Creating a child PR

1. Create child branch from the parent: `git checkout -b feat/child-task feat/parent-feature`
2. Set `gh pr create --base feat/parent-feature`
3. After all children are merged into parent, create the final PR: `gh pr create --base develop`

## Review Checklist
- [ ] Code follows clean architecture layer rules
- [ ] No business logic in Composables or Controllers
- [ ] New public functions have clear naming (no abbreviations)
- [ ] No hardcoded strings (use resources/constants)
- [ ] Error cases handled
- [ ] Tests cover the happy path and at least one failure case

## Automated Code Review

Every PR goes through an automated deep review (`/review`) before merge. This replaces manual GitHub review.

### Process
1. All changed files are reviewed against 6 categories: **BUG**, **SEC**, **ARCH**, **PERF**, **TEST**, **STYLE**
2. Each finding is classified: **BLOCKER** (must fix), **WARNING** (should fix), **NIT** (optional)
3. Findings are posted as a PR comment tagging `@gm-solvd` with the total problem count
4. Only BLOCKERs and WARNINGs count toward the problem count — NITs never block merge

### Auto-merge
- **0 problems** → PR is approved and merged via `gh pr merge --squash --delete-branch`
- **Problems found** → auto-fix, validate, commit, push, re-review (max 3 cycles)
- **3 cycles exhausted** → escalate to user for manual resolution

### Severity Guide

| Severity | Examples | Blocks? |
|----------|---------|---------|
| BLOCKER | Bugs, security issues, architecture violations | Yes |
| WARNING | Missing tests, performance concerns, unclear naming | Yes |
| NIT | Style preferences, minor improvements | No |
