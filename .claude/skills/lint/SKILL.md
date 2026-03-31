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

2. Run lint:
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintCheck
   ```

3. Parse the output and report:
   - Total number of issues found
   - File and line number for each issue
   - Rule that was violated

4. If issues are found, suggest fixes

5. If user approves auto-fixing:
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintFormat
   ```
