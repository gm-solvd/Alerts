---
name: lint
description: Run linting checks on the codebase
disable-model-invocation: true
allowed-tools: Bash, Read
argument-hint: [api|mobile]
---

Run lint checks and report results.

## Steps

1. Determine target from `$ARGUMENTS` (default: `api`)

2. Auto-format first:
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintFormat
   ```

3. Verify lint passes:
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintCheck
   ```

4. If `ktlintCheck` still fails after format (e.g., lines exceeding 140 chars that cannot be auto-corrected):
   - Parse the output and report file, line, and rule for each issue
   - Manually fix the issues (break long lines, refactor expressions)
   - Re-run `ktlintCheck` to verify

5. Report results:
   - Number of issues auto-fixed by `ktlintFormat`
   - Number of issues remaining (if any)
   - PASS or FAIL

## Important
- **Always run `ktlintFormat` before `ktlintCheck`** — most violations are auto-fixable
- Common violations: import ordering, multiline expressions, parameter newlines, blank lines after class opening, max line length (140 chars)
- The CI pipeline runs `ktlintCheck` — code must pass before pushing
