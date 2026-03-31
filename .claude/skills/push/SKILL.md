---
name: push
description: Push current branch to remote
disable-model-invocation: true
allowed-tools: Bash
---

Push the current branch to the remote repository.

## Steps

1. Run `git branch --show-current` to identify the current branch
2. Run `git status` to check for uncommitted changes
   - If there are uncommitted changes, **STOP** and warn the user
3. Run `git log --oneline -5` to show recent commits that will be pushed
4. Check if the branch tracks a remote:
   ```
   git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
   ```
5. If no remote tracking branch exists:
   ```
   git push -u origin HEAD
   ```
6. If remote tracking exists:
   ```
   git push
   ```
7. Confirm push was successful with `git log --oneline origin/$(git branch --show-current) -3`

## Safety

- NEVER force push (`--force` or `-f`)
- NEVER push to `main` or `develop` directly — warn the user if on those branches
- If push is rejected, explain why and suggest `git pull --rebase` first
