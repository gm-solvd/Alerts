# Rules

Architecture decisions, workflow requirements, and coding standards for the Privacy Alert System. All contributors (human and AI) must follow these rules.

## Sub-directories

| Directory | Purpose |
|-----------|---------|
| [`general/`](general/SUMMARY.md) | Workflow, branching, commit format, PR guidelines, progress tracking |
| [`architecture-api/`](architecture-api/SUMMARY.md) | Clean Architecture rules for the Spring Boot API: layers, auth, testing, scoring, error handling, database |
| [`architecture-mobile/`](architecture-mobile/SUMMARY.md) | KMP + Compose Multiplatform architecture rules (mobile not yet started) |

## Quick Reference

- Commit format: `<type>(<scope>): <description>` — see [`general/commits.md`](general/commits.md)
- Branch strategy: `feat/*` and `fix/*` off `develop` — see [`general/workflow.md`](general/workflow.md)
- Implementation steps: 9-step workflow with STOP checkpoints — see [`general/implementation-workflow.md`](general/implementation-workflow.md)
- Layer rules: domain has zero external deps — see [`architecture-api/clean-architecture.md`](architecture-api/clean-architecture.md)
