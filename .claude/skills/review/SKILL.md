---
name: review
description: Review current branch's PR, post findings, auto-merge or auto-fix loop
allowed-tools: Bash, Read, Grep, Edit, Write, Agent
argument-hint: [PR number, default: current branch's PR]
---

Run a deep code review on the current PR and either merge or fix issues.

## Prerequisites

- A PR must exist for the current branch. If not, STOP and tell the user to run `/pr` first.
- `gh` CLI must be available at `/opt/homebrew/bin/gh`

---

## Step 1: Gather Context

1. Send notification:
   ```bash
   osascript -e 'display notification "Starting automated code review..." with title "Claude Code" subtitle "Review" sound name "Glass"'
   ```

2. Identify the PR:
   ```bash
   /opt/homebrew/bin/gh pr view --json number,title,baseRefName,headRefName,url
   ```
   If `$ARGUMENTS` is a PR number, use `/opt/homebrew/bin/gh pr view $ARGUMENTS` instead.

3. Get the full diff:
   ```bash
   /opt/homebrew/bin/gh pr diff
   ```

4. Get the list of changed files:
   ```bash
   /opt/homebrew/bin/gh pr diff --name-only
   ```

5. Read each changed file in full to understand context (not just the diff hunks).

6. Determine which platform is affected:
   - Files under `api/` → read `rules/architecture-api/` rules
   - Files under `mobile/` → read `rules/architecture-mobile/` rules
   - Both → read both

---

## Step 2: Deep Review

Review every changed file against these six categories. Be thorough — review like a senior engineer who is a specialist in the platform (Kotlin/Spring for API, KMP/Compose for mobile).

### BUG — Correctness
- Null safety violations, potential NPEs
- Off-by-one errors in loops, pagination, indexing
- Resource leaks (unclosed streams, connections, coroutine scope leaks)
- Wrong return values, swapped parameters
- Missing edge cases (empty lists, zero values, boundary conditions)
- Race conditions, thread safety issues
- Incorrect error propagation

### SEC — Security
- Auth bypass, missing authorization checks
- Injection vulnerabilities (SQL, command, XSS)
- Hardcoded secrets, tokens, API keys
- JWT misuse (wrong algorithm, missing validation, token exposure in logs)
- Sensitive data in error messages or responses
- CORS misconfiguration

### ARCH — Architecture
- Layer violations (domain importing Spring/JPA/Ktor/Compose)
- Business logic in controllers, composables, or ViewModels
- DTOs leaking into domain, entities leaking into API
- Missing interface abstractions (concrete dependencies in domain)
- Incorrect dependency injection scoping (singleton vs factory)
- Violation of unidirectional data flow in mobile

### PERF — Performance
- N+1 query patterns
- Unbounded fetches (missing pagination or limits)
- Unnecessary object allocations in hot paths
- Missing indexes for query patterns
- Blocking calls on main thread (mobile)
- Redundant network calls, missing caching where appropriate

### TEST — Testing
- Missing test coverage for new public methods
- Wrong assertions (asserting on the wrong value)
- Missing edge case tests (error paths, empty inputs, boundaries)
- Test naming not following convention: `` `action condition expected` ``
- Tests that don't actually test anything (always pass)

### STYLE — Code Quality
- Naming: unclear, abbreviated, or misleading names
- Dead code, unused imports, commented-out code
- Hardcoded strings that should be constants/resources
- Line length exceeding 140 chars
- Missing or incorrect conventional commit format
- Inconsistent patterns compared to rest of codebase

---

## Step 3: Classify Findings

Assign each finding a severity:

| Severity | Meaning | Blocks merge? |
|----------|---------|---------------|
| **BLOCKER** | Bug, security issue, or architecture violation that will cause problems | Yes |
| **WARNING** | Significant issue that should be fixed but won't break anything immediately | Yes |
| **NIT** | Style preference, minor improvement — nice to have | No |

Count: `problems = BLOCKER count + WARNING count`

---

## Step 4: Post Review Comment

Post a single structured comment on the PR:

```bash
/opt/homebrew/bin/gh pr comment $PR_NUMBER --body "$(cat <<'EOF'
## Code Review

**Reviewed:** N files, M lines changed
**Platform:** [API / Mobile / Both]

### Findings

#### BLOCKER (X)
- **[CATEGORY]** `file.kt:line` — Description
  > Suggestion: how to fix

#### WARNING (Y)
- **[CATEGORY]** `file.kt:line` — Description
  > Suggestion: how to fix

#### NIT (Z)
- **[CATEGORY]** `file.kt:line` — Description
  > Suggestion: ...

---
@gm-solvd {X + Y} problems found
EOF
)"
```

If zero findings across all categories, use this format instead:
```
## Code Review

**Reviewed:** N files, M lines changed
**Platform:** [API / Mobile / Both]

No issues found. Code follows architecture rules, no bugs detected, security looks good.

---
@gm-solvd 0 problems found
```

---

## Step 5: Decision & Action

### If 0 problems (blockers + warnings):

**MANDATORY: Run local tests before merging — never skip this.**

1. Run validation based on what platform the PR touches:
   - For API changes: `cd /Users/gmribas/Projects/Alerts/api && JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew compileKotlin ktlintCheck test`
   - For Mobile changes: `cd /Users/gmribas/Projects/Alerts/mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:compileDebugKotlinAndroid :shared:allTests`
   - For both: run both

2. If tests fail: treat each failing test as a **BLOCKER**, fix them, then go to "If problems found" flow below.

3. If tests pass, merge the PR:
   ```bash
   /opt/homebrew/bin/gh pr merge $PR_NUMBER --squash --delete-branch
   ```

4. Switch to base branch and pull:
   ```bash
   git checkout <base-branch> && git pull origin <base-branch>
   ```

5. Send notification:
   ```bash
   osascript -e 'display notification "PR merged. 0 problems found." with title "Claude Code" subtitle "Review Complete" sound name "Glass"'
   ```

### If problems found (iteration < 3):

1. Send notification:
   ```bash
   osascript -e 'display notification "Review found N problems. Auto-fixing..." with title "Claude Code" subtitle "Review: Fixing" sound name "Glass"'
   ```

2. Fix each BLOCKER and WARNING:
   - Read the affected file
   - Apply the fix
   - Verify the fix doesn't break surrounding code

3. Run validation (mandatory, same as above):
   - For API: `cd /Users/gmribas/Projects/Alerts/api && JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew compileKotlin ktlintFormat ktlintCheck test`
   - For Mobile: `cd /Users/gmribas/Projects/Alerts/mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:compileDebugKotlinAndroid :shared:allTests :androidApp:assembleDebug`

4. Commit the fixes:
   - Stage files by name (never `git add .`)
   - Commit: `fix(<scope>): address review findings — <brief description>`
   - Push: `git push`

5. **Go back to Step 1** (re-review the updated PR)

### If problems found (iteration >= 3):

1. Send notification:
   ```bash
   osascript -e 'display notification "Review: 3 cycles exhausted. Manual review needed." with title "Claude Code" subtitle "Review: Blocked" sound name "Glass"'
   ```

2. **STOP** — Present all remaining findings to the user for manual resolution.

---

## Step 6: Triage NITs for Later Improvements

After a successful merge, if any NIT findings were posted:

1. **Review each NIT comment** and evaluate whether it deserves a "later improvement" entry.

2. **Criteria for adding to later improvements:**
   - The NIT reveals a pattern that should be addressed across the codebase (not just one spot)
   - The NIT identifies tech debt that could cause issues as the codebase grows
   - The NIT suggests a refactor that would improve maintainability but isn't urgent
   - The NIT identifies missing test coverage for non-critical paths

3. **Do NOT add to later improvements if:**
   - The NIT is purely cosmetic and already passing detekt/lint
   - The NIT is a subjective style preference with no clear benefit
   - The NIT was already fixed as part of a BLOCKER/WARNING fix

4. **For qualifying NITs**, append to `rules/general/later-improvements.md`:
   ```markdown
   - [ ] **[CATEGORY]** `file.kt:line` — Description (from PR #N review)
   ```

---

## Step 7: Post-Review Housekeeping

After a successful merge:

1. **Permission tracking**: Check if any bash commands during this session required user permission grants that are not yet in `.claude/settings.json` or `.claude/settings.local.json`. If found, add them.

2. **Continue workflow**: If this review was part of a larger workflow (e.g., `/workflow`), proceed to the next step (Update Docs & Progress).

---

## Rules

- **Never skip the review** — every PR gets reviewed before merge
- **Never merge without running tests locally** — code review alone is insufficient; CI may be slow or pending. Always run `./gradlew test` (API) and/or `./gradlew :shared:allTests` (mobile) before merging. A review that found 0 problems means nothing if tests break.
- **NITs never block** — only BLOCKERs and WARNINGs count toward the problem count
- **Max 3 iterations** — prevents infinite fix-review loops
- **Always post as PR comment** — creates a persistent record on GitHub
- **Re-read architecture rules each iteration** — don't rely on memory from previous cycle
- **Read full files, not just diffs** — context around changed code matters
- **If merge fails** (branch protection, conflicts, etc.) — STOP and ask user
- **No self-approval** — GitHub blocks `gh pr review --approve` on own PRs. Skip approve, go straight to merge.
- **osascript strings cannot contain `#`** — use the word "number" instead of `#` in notification messages
