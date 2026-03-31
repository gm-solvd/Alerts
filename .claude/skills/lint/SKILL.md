---
name: lint
description: Run linting checks on the codebase
disable-model-invocation: true
allowed-tools: Bash, Read
---

Run lint checks and report results.

## Steps

1. Run `./gradlew ktlintCheck` for the full project
   - Or scope to a module if `$ARGUMENTS` is provided (e.g., `./gradlew :backend:ktlintCheck`)
2. Parse the output and report:
   - Total number of issues found
   - File and line number for each issue
   - Rule that was violated
3. If issues are found, suggest fixes
4. Optionally run `./gradlew ktlintFormat` if the user approves auto-fixing
