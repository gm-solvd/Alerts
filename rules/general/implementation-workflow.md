# Implementation Workflow

AI-assisted development workflow with explicit human checkpoints.

---

## Steps

### 1. Task Intake
- AI reads task from issue tracker (via MCP)
- **STOP** — Confirm task understanding

### 2. Branch Creation
- AI proposes branch name following conventions
- **STOP** — Approve branch name

### 3. Planning
- AI reads project rules and relevant documentation
- AI creates implementation plan (files, steps, tests)
- **STOP** — Review and approve plan

### 4. Implementation
- AI implements step by step
- NO COMMITS at this stage
- All changes visible in `git status` for your review

### 5. Validation
- AI compiles: `./gradlew compileKotlin`
- AI auto-formats: `./gradlew ktlintFormat`
- AI verifies lint: `./gradlew ktlintCheck` (manually fix any remaining issues)
- **Mobile**: AI runs detekt: `./gradlew :shared:detektMetadataCommonMain` — fixes unused imports/code
- AI runs tests: `./gradlew test`
- If failures — AI fixes and re-validates
- **STOP** — Report validation results, wait for review

### 6. Human Review
- YOU review all changes in IDE
- Request adjustments if needed
- Approve when satisfied

### 7. Atomic Commits
- AI groups changes into logical, atomic commits
- Follows commit message conventions
- Stage files by name (never `git add .` or `git add -A`)
- Each commit must be independently valid

### 8. Push & Pull Request
- AI pushes branch to remote: `git push -u origin HEAD`
- AI drafts PR title and description following `rules/general/pr-guidelines.md`
- **STOP** — Review PR draft
- AI creates PR via `gh pr create --base develop`
- AI returns the PR URL
- **CI runs automatically** — GitHub Actions validates the PR (compile, lint, test)
- **If CI fails** — Claude automatically analyzes the failure logs and pushes a fix to the PR branch (via `pr-autofix.yml`). No manual intervention needed.
- PR proceeds to automated review in Step 8.5

### 8.5. Code Review & Auto-Merge
- AI runs `/review` on the PR
- Deep review against 6 categories: BUG, SEC, ARCH, PERF, TEST, STYLE
- Findings posted as PR comment tagging @gm-solvd with problem count
- **0 problems** → auto-approve and merge via `gh pr merge --squash --delete-branch`
- **Problems found** → auto-fix, validate, commit, push, re-review (max 3 cycles)
- **3 cycles exhausted** → **STOP** for manual resolution
- macOS notifications sent at each stage

### 9. Update Progress
- AI updates `rules/general/progress.md` with completed work
- Commits and pushes the progress update

### 10. Later Improvements
- After **all tasks** in the feature are merged (not after each individual task)
- AI reads `rules/general/later-improvements.md`
- For each pending item: branch → implement → validate → commit → PR → review
- Items are added during Step 8.5 review when NIT findings reveal patterns worth addressing
