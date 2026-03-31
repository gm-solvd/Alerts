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
- AI runs linting, tests, builds locally
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
- PR is **not auto-merged** — requires manual review and approval

### 9. Update Progress
- AI updates `rules/general/progress.md` with completed work
- Commits and pushes the progress update
