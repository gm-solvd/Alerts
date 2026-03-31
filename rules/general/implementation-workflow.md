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
- Pushes to remote

### 8. Pull Request
- AI drafts PR title and description
- **STOP** — Review PR draft
- AI creates PR via CLI
