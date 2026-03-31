---
name: pr
description: Create a pull request following project conventions
disable-model-invocation: true
allowed-tools: Bash, Read, Grep
---

Create a pull request following the project's PR guidelines.

## Steps

1. Run `git status` to check for uncommitted changes (warn if any)
2. Run `git log --oneline develop..HEAD` to see all commits in this branch
3. Run `git diff develop...HEAD` to understand the full changeset
4. Check remote tracking: `git rev-list --left-right --count develop...HEAD`
5. Push to remote if needed: `git push -u origin HEAD`
6. Draft PR title: `<type>(<scope>): <short description>` (under 70 chars)
7. Draft PR body using this template:

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

8. **STOP** — Show PR draft to user for review
9. After approval, create PR via:
```
gh pr create --base develop --title "<title>" --body "<body>"
```
10. Return the PR URL
