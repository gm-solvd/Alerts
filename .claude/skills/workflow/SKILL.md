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
- Related tasks use parent/child branches: children merge to parent, parent merges to develop when complete
- **STOP** — Present the task split for confirmation before proceeding

---

## Step 1: Task Intake

- Send notification:
  ```bash
  osascript -e 'display notification "Starting task intake..." with title "Claude Code" subtitle "Step 1: Intake" sound name "Glass"'
  ```
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

- Send notification:
  ```bash
  osascript -e 'display notification "Planning phase starting..." with title "Claude Code" subtitle "Step 3: Planning" sound name "Glass"'
  ```
- Enter plan mode
- Read relevant source files to understand current state
- Read relevant architecture rules
- Create a detailed implementation plan: files to create/modify, steps, tests needed
- **STOP** — Present plan for approval via ExitPlanMode

## Step 4: Implementation

- Send notification:
  ```bash
  osascript -e 'display notification "Implementation starting..." with title "Claude Code" subtitle "Step 4: Implement" sound name "Glass"'
  ```
- Implement the plan step by step
- Follow clean architecture rules strictly
- **NO COMMITS** at this stage
- All changes must be visible in `git status`
- If the plan needs adjustment during implementation, explain why and get approval

## Step 5: Validation

> **Config-only changes skip this step.** If every changed file is under `.claude/`, `rules/`, or is `CLAUDE.md` — skip compile, lint, and test entirely. Jump straight to Step 7.

- Send notification:
  ```bash
  osascript -e 'display notification "Running validation pipeline..." with title "Claude Code" subtitle "Step 5: Validate" sound name "Glass"'
  ```
- **API changes:**
  - Run: `cd api && ./gradlew compileKotlin`
  - Run: `cd api && ./gradlew ktlintFormat` to auto-fix lint issues
  - Run: `cd api && ./gradlew ktlintCheck` to verify lint passes (manually fix any remaining issues like lines exceeding 140 chars)
  - Run: `cd api && ./gradlew test` — this runs **all tests** including unit tests and integration tests (Testcontainers PostgreSQL)
  - If any integration test was added or modified, update `api/TEST_REPORT.md`
- **Mobile changes:**
  - Run: `cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:compileDebugKotlinAndroid`
  - Run: `cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:detektMetadataCommonMain`
  - If detekt fails: read report, fix issues (remove unused code, fix violations), re-run until clean
  - Run: `cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:allTests` (if tests exist)
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

- Send notification:
  ```bash
  osascript -e 'display notification "Pushing branch and creating PR..." with title "Claude Code" subtitle "Step 8: Push & PR" sound name "Glass"'
  ```
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
- PR is **not auto-merged** — it goes through the automated review in Step 8.5

## Step 8.5: Code Review & Merge

- Run `/review` on the PR just created
- The review skill will:
  1. Deep review all changed files against architecture rules (BUG, SEC, ARCH, PERF, TEST, STYLE)
  2. Post findings as a PR comment tagging @gm-solvd with problem count
  3. If 0 problems: approve and merge the PR via `gh pr merge --squash --delete-branch`
  4. If problems found: auto-fix, validate, commit, push, re-review (max 3 cycles)
  5. Send macOS notifications at each stage
- After merge, switch to base branch and pull latest
- If review exhausts 3 cycles without resolving all issues, **STOP** for manual resolution

## Step 9: Update Docs & Progress

- Run `/docs` to update `SUMMARY.md` files for any changed directories
- Update `README.md` to reflect any new features, endpoints, or architectural changes
- Update `rules/general/progress.md` with the completed work
- Commit and push these updates on the feature branch before the PR
- Send notification:
  ```bash
  osascript -e 'display notification "Phase merged. Moving to next phase..." with title "Claude Code" subtitle "Workflow: Next Phase" sound name "Glass"'
  ```
- **DO NOT STOP** — immediately proceed to the next pending phase in `rules/general/progress.md`
- Repeat from Step 1 for the next phase (branch off develop, plan, implement, validate, PR, review)
- Continue until **all phases** are DONE or the user explicitly asks to stop

## Step 10: All Phases Complete

When all phases in `rules/general/progress.md` are marked DONE:

1. Read `rules/general/later-improvements.md`
2. If there are pending items:
   - Send notification:
     ```bash
     osascript -e 'display notification "Starting later improvements phase..." with title "Claude Code" subtitle "Step 10: Improvements" sound name "Glass"'
     ```
   - For each item, follow the normal workflow: branch → implement → validate → commit → PR → review
   - Check off completed items in `later-improvements.md`
3. Only after all phases AND all later-improvement items are finished, send the **prominent** completion notification:
   ```bash
   osascript -e 'display dialog "ALL DONE! Every phase and improvement has been implemented, validated, and merged." with title "Claude Code — DONE" with icon note buttons {"OK"} default button "OK"'
   ```
4. If no items, skip this step

---

## Multi-Phase / Parent-Child Workflows

When a feature has multiple related tasks, use a **parent/child** branch pattern:

1. **Parent branch** — created from `develop` (e.g., `feat/mobile-phase1-foundation`)
2. **Child branches** — created from the parent (e.g., `feat/mobile-network-layer`)
3. Each child's PR targets the **parent branch** (`gh pr create --base feat/parent-feature`)
4. After **all children** are merged into the parent, the parent PR targets `develop`
5. Branch names must be **relevant** — clearly describe what the branch does
