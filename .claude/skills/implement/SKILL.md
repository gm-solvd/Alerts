---
name: implement
description: Implement a planned feature step by step
disable-model-invocation: true
allowed-tools: Bash, Read, Grep, Edit, Write, Agent
argument-hint: [feature or task description]
---

Implement: $ARGUMENTS

This skill handles **Step 4 (Implementation)** of the workflow. A plan should already be approved before running this.

## Rules

1. **Read the plan** — Check the current plan file in `.claude/plans/` for the approved implementation plan
2. **Read the architecture rules** relevant to the work:
   - API work: `rules/architecture-api/`
   - Mobile work: `rules/architecture-mobile/`
   - General: `rules/general/`
3. **Implement step by step** following the plan order
4. **NO COMMITS** — Do not commit anything. All changes stay in working directory.
5. **Follow clean architecture strictly:**
   - Domain layer: pure Kotlin, no framework annotations (except `@Service`)
   - Data layer: JPA entities, map to/from domain via extension functions
   - API layer: thin controllers, separate Request/Response DTOs
   - Config layer: Spring beans, security, infrastructure implementations
6. **Follow existing patterns** — Read similar existing files before creating new ones to match style
7. After implementation, run `git status` to show all changes
8. **STOP** — Report what was implemented and wait for the user to proceed to validation

## What Comes Next

After implementation is approved, the remaining workflow steps are:
- **Validation** — `./gradlew compileKotlin && ./gradlew ktlintFormat && ./gradlew ktlintCheck && ./gradlew test`
- **Human Review** — User reviews `git diff` in IDE
- **Atomic Commits** — Stage files by name, group logically, push to remote
- **Pull Request** — Create PR via `gh pr create --base develop` (see `/pr` skill)
- **Update Progress** — Update `rules/general/progress.md`
