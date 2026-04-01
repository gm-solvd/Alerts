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
3. **Fetch remote state before pushing:**
   ```bash
   git fetch origin
   ```
4. Check if the branch tracks a remote:
   ```bash
   git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
   ```
5. **If the branch already has a remote tracking branch** (i.e., PR already exists):
   - Check for remote commits not yet in local: `git log HEAD..origin/<branch> --oneline`
   - If remote has new commits, rebase before pushing:
     ```bash
     git pull --rebase origin <branch>
     ```
6. Run `git log --oneline -5` to show recent commits that will be pushed
7. Push:
   - No remote tracking: `git push -u origin HEAD`
   - Remote tracking exists: `git push`
8. Confirm push was successful with `git log --oneline origin/$(git branch --show-current) -3`

## Safety

- NEVER force push (`--force` or `-f`)
- NEVER push to `main` or `develop` directly — warn the user if on those branches
- If push is rejected after fetch+rebase, stop and explain — do not force push
