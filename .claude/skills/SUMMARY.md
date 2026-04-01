# Claude Code Skills

Custom automation skills for the Privacy Alert System workflow. Invoked with `/skill-name` in Claude Code.

## Skills

| Skill | Trigger | Description |
|-------|---------|-------------|
| `workflow` | `/workflow` | Runs the full 9-step implementation workflow: intake, branch, plan, implement, validate, commit, PR, progress |
| `branch` | `/branch <type>/<scope>-<desc>` | Creates a feature/fix branch off `develop` with correct naming convention |
| `commit` | `/commit` | Stages changed files by name (never `git add .`), formats commit message per convention, creates commit |
| `pr` | `/pr` | Pushes branch, creates GitHub PR targeting `develop`, returns PR URL |
| `validate` | `/validate` | Runs `compileKotlin`, `ktlintFormat`, `ktlintCheck`, `test` — reports results |
| `test` | `/test [filter]` | Runs test suite with optional class/method filter |
| `lint` | `/lint` | Runs `ktlintCheck` and optionally `ktlintFormat` |
| `push` | `/push` | Pushes current branch to origin |
| `implement` | `/implement <step>` | Implements a specific plan step, then runs validate |
| `progress` | `/progress <platform> <phase> <status>` | Updates `rules/general/progress.md` status table |
| `docs` | `/docs` | Updates `SUMMARY.md` files for any directories whose source files changed, then updates `README.md` links |
