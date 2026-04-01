---
name: pr
description: Create a pull request following project conventions
disable-model-invocation: true
allowed-tools: Bash, Read, Grep
argument-hint: [base branch, default: develop]
---

Create a pull request following the project's PR guidelines.

## Steps

1. Run `git status` to check for uncommitted changes (warn if any)
2. Determine base branch: `$ARGUMENTS` or default to `develop`
3. Run `git log --oneline $BASE..HEAD` to see all commits in this branch
4. Run `git diff $BASE...HEAD` to understand the full changeset
5. Push to remote if needed: `git push -u origin HEAD`
6. Draft PR:

### Title
`<type>(<scope>): <short description>` — under 70 chars

### Body
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

7. **STOP** — Show PR draft to user for review
8. After approval, create PR:
   ```
   gh pr create --base $BASE --title "<title>" --body "$(cat <<'EOF'
   <body content>
   EOF
   )"
   ```
9. Return the PR URL
10. CI validates the PR automatically via GitHub Actions (compile, lint, test)
11. If CI fails, Claude automatically analyzes the failure logs and pushes a fix to the PR branch
12. PR is **not auto-merged** — requires manual review and approval

## Dependent / Stacked PRs

When a feature is split into phases (e.g., backend + frontend), create **stacked PRs**:

1. **Phase 1 PR** — base: `develop` (as usual)
2. **Phase 2 PR** — base: **Phase 1's branch** (not develop)

### How to create a dependent PR

- Set `--base` to the **parent branch name** (e.g., `feat/scan-async-backend`)
- In the PR body, add a **Dependencies** section:
  ```markdown
  ## Dependencies
  - Depends on #<parent-PR-number> — must be merged first
  ```
- GitHub will show the diff **only between the two feature branches** (not the full diff from develop)

### Merge order

1. Merge Phase 1 into develop first
2. After merge, **rebase Phase 2 onto develop**: `git rebase develop`
3. Update Phase 2 PR base to `develop` (via GitHub UI or `gh pr edit --base develop`)
4. Then merge Phase 2

### Rules for stacked PRs

- Each PR must be **independently reviewable** — clear scope and test plan
- Phase 2 branch is created **from Phase 1's branch** (not from develop)
- If Phase 1 changes after Phase 2 is created, rebase Phase 2 onto Phase 1

## Rules
- **One concern per PR** — avoid mixing unrelated changes
- **Never force-push** to shared branches
- **Squash merge** preferred
