---
name: workflow
description: Run the full implementation workflow for a feature or task
disable-model-invocation: true
allowed-tools: Bash, Read, Grep, Edit, Write, Agent
argument-hint: [task description or issue number]
---

Execute the full implementation workflow for: $ARGUMENTS

Follow these steps **in order**. Each **STOP** point requires explicit user approval before proceeding.

> ⚠️ **Never commit directly to `develop` or `main`.** All commits must go to a feature/fix branch.

### Terminology
- **Feature** — the top-level goal being implemented (what `$ARGUMENTS` describes)
- **Task** — a workstream within the feature (Web Backend, Web Frontend, Mobile UI, Mobile Business Logic)

---

## Step 0: Workstream Split

Before any planning, divide the feature into tasks:

| Workstream | Applies when |
|------------|-------------|
| **Web Backend** | Kotlin/Spring changes (domain, data, api, integration, config) |
| **Web Frontend** | Vue admin dashboard changes |
| **Mobile UI** | Compose Multiplatform UI layer |
| **Mobile Business Logic** | KMP shared domain / use-cases |

- List which workstreams are affected
- Each active workstream becomes a separate task (branch + PR)
- Tasks that depend on each other become stacked PRs (backend first, then frontend)
- **STOP** — Present the task split for confirmation before proceeding

---

## Step 1: Task Intake

- Read the task description from `$ARGUMENTS`
- If it's an issue number, fetch it with `gh issue view $ARGUMENTS`
- Read all relevant project rules from `rules/`
- Summarize your understanding of the task: **what** needs to be done, **why**, and **which layers** are affected (domain, data, api, config, integration)
- **STOP** — Present your understanding and wait for confirmation

## Step 2: Branch Creation

- Determine the correct branch type from the task:
  - `feat/` for new features
  - `fix/` for bug fixes
  - `refactor/` for refactoring
  - `test/` for test additions
  - `docs/` for documentation
  - `chore/` for tooling/deps
- Use the scopes: `mobile`, `android`, `ios`, `api`, `db`, `auth`, `alerts`, `score`, `scan`
- Propose a branch name: `<type>/<scope>-<short-description>`
- **STOP** — Present branch name for approval
- After approval, **sync develop before branching**:
  ```bash
  git fetch origin
  git checkout develop
  git pull origin develop
  git checkout -b <branch-name>
  ```

## Step 3: Planning

- Enter plan mode
- Read relevant source files to understand current state
- Read relevant architecture rules
- Create a detailed implementation plan: files to create/modify, steps, tests needed
- **STOP** — Present plan for approval via ExitPlanMode

## Step 4: Implementation

- Implement the plan step by step
- Follow clean architecture rules strictly
- **NO COMMITS** at this stage
- All changes must be visible in `git status`
- If the plan needs adjustment during implementation, explain why and get approval

## Step 5: Validation

- **API changes:**
  - Run: `cd api && ./gradlew compileKotlin`
  - Run: `cd api && ./gradlew ktlintFormat` to auto-fix lint issues
  - Run: `cd api && ./gradlew ktlintCheck` to verify lint passes (manually fix any remaining issues like lines exceeding 140 chars)
  - Run: `cd api && ./gradlew test` — this runs **all tests** including unit tests and integration tests (Testcontainers PostgreSQL)
  - If any integration test was added or modified, update `api/TEST_REPORT.md`
- **Mobile changes:**
  - Run: `cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:compileKotlinMetadata`
  - Run: `cd mobile && ./gradlew :androidApp:assembleDebug`
  - Run: `cd mobile && ./gradlew :shared:allTests` (if tests exist)
- If any step fails, fix the issue and re-validate
- **STOP** — Report validation results (unit pass count, integration pass count) and wait for review

## Step 6: Human Review

- Run `git diff` to show all changes
- Remind the user to review changes in their IDE
- Wait for approval or adjustment requests
- Apply any requested changes, then re-validate

## Step 7: Atomic Commits

- Group changes into logical, atomic commits
- Each commit should be independently valid
- Use conventional commit format with correct type and scope
- Stage files by name (never `git add .` or `git add -A`)

## Step 8: Push & Pull Request

- Push to remote: `git push -u origin HEAD`
- Read `rules/general/pr-guidelines.md` for PR conventions
- Draft PR title: `<type>(<scope>): <short description>` (under 70 chars)
- Draft PR body using the project template:
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
  ```
- **STOP** — Show PR draft for approval
- After approval: create PR with `gh pr create --base develop`
- Return the PR URL to the user
- CI validates the PR automatically (compile, lint, test via GitHub Actions)
- If CI fails, Claude automatically analyzes the failure and pushes a fix (via `pr-autofix.yml`)
- PR is **not auto-merged** — it requires manual review and approval before merging

## Step 9: Update Docs & Progress

- Run `/docs` to update `SUMMARY.md` files for any changed directories
- Update `README.md` to reflect any new features, endpoints, or architectural changes
- Update `rules/general/progress.md` with the completed work
- Commit and push these updates on the feature branch before the PR

---

## Multi-Phase / Parallel Workflows

When a feature is split into independent phases (e.g., backend + frontend):

1. **Phase 1 branch** — created from `develop` as usual
2. **Phase 2 branch** — created from **Phase 1's branch** (not develop)
3. Both phases can be **implemented in parallel** (using worktrees or agents)
4. **Phase 1 PR** → base: `develop`
5. **Phase 2 PR** → base: Phase 1's branch (dependent PR)
6. Add `## Dependencies` section in Phase 2's PR body linking to Phase 1's PR number
7. **Merge order**: Phase 1 first, then rebase Phase 2 onto develop, update base, merge
