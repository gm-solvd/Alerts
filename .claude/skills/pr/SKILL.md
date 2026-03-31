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
11. PR is **not auto-merged** — requires manual review and approval

## Rules
- **One concern per PR** — avoid mixing unrelated changes
- **Never force-push** to shared branches
- **Squash merge** preferred
