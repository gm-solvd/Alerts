---
name: branch
description: Create a new branch following project conventions
disable-model-invocation: true
allowed-tools: Bash, Read
argument-hint: [type/scope-description]
---

Create a new git branch for: $ARGUMENTS

## Steps

1. Run `git branch --show-current` to confirm you're on the right base branch
2. Run `git status` to check for uncommitted changes (warn if any)
3. If `$ARGUMENTS` already looks like a valid branch name (e.g., `feat/api-auth-flow`), use it directly
4. Otherwise, parse `$ARGUMENTS` and construct a branch name:

### Branch naming convention
```
<type>/<scope>-<short-description>
```

**Types:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

**Scopes:** `api`, `mobile`, `android`, `ios`, `db`, `auth`, `alerts`, `score`, `scan`

**Examples:**
- `feat/api-auth-flow`
- `fix/alerts-null-response`
- `refactor/score-calculation`

5. **STOP** — Present the branch name for approval
6. After approval: `git checkout -b <branch-name>`
7. Confirm with `git branch --show-current`
