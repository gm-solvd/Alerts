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
5. **If the branch already has a remote tracking branch** (i.e., PR may exist):
   - **Check if the PR is still open before pushing:**
     ```bash
     gh pr view --json state,number,title 2>/dev/null
     ```
   - If the PR state is `MERGED` or `CLOSED` — **STOP**. Warn the user: the PR is already merged/closed. Do not push. Ask what to do next (open a new PR? push to develop directly? create a new branch?).
   - If the PR is `OPEN`, check for remote commits not yet in local:
     ```bash
     git log HEAD..origin/<branch> --oneline
     ```
   - If remote has new commits, rebase before pushing:
     ```bash
     git pull --rebase origin <branch>
     ```
6. Run `git log --oneline -5` to show recent commits that will be pushed
7. Push:
   - No remote tracking: `git push -u origin HEAD`
   - Remote tracking exists and PR is open: `git push`
8. Confirm push was successful with `git log --oneline origin/$(git branch --show-current) -3`

## Safety

- NEVER force push (`--force` or `-f`) unless rebasing was just performed and the user confirms
- NEVER push to `main` or `develop` directly — warn the user if on those branches
- NEVER push to a branch whose PR is already merged or closed — stop and ask the user
- If push is rejected after fetch+rebase, stop and explain — do not force push
