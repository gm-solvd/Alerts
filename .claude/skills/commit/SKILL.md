---
name: commit
description: Stage and commit changes following project conventions
disable-model-invocation: true
allowed-tools: Bash, Read, Grep
---

Create a git commit following the project's conventional commit format.

## Steps

1. Run `git status` to see all changes (never use `-uall`)
2. Run `git diff` to review staged and unstaged changes
3. Run `git log --oneline -5` to see recent commit style
4. Analyze changes and determine the correct **type** and **scope**

### Types
| Type | When |
|------|------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change that is not a fix or feature |
| `test` | Adding or updating tests |
| `docs` | Documentation only |
| `chore` | Build, tooling, dependency updates |
| `style` | Formatting, no logic change |
| `perf` | Performance improvement |

### Scopes
`mobile`, `android`, `ios`, `api`, `db`, `auth`, `alerts`, `score`, `scan`

5. Stage relevant files by name — **NEVER** use `git add -A` or `git add .`
6. **NEVER** commit files containing secrets (`.env`, credentials, API keys)
7. Write commit message:
   - Subject: imperative mood, max 72 chars, no period
   - Body: explain *why*, not *what*
8. Use a HEREDOC for the message:
   ```
   git commit -m "$(cat <<'EOF'
   <type>(<scope>): <short description>

   [optional body]

   Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
   EOF
   )"
   ```
9. Run `git status` after commit to verify success

## Rules

- **Never commit to `develop` or `main` directly** — only commit on feature/fix branches. Exception: user explicitly requests it.
- Stage files by name — never `git add -A` or `git add .`
- Never commit secrets (`.env`, credentials, API keys)

## Grouping

If there are changes across multiple concerns, create **multiple atomic commits** — one per logical change. Each commit should be independently valid.
