---
name: validate
description: Run full validation (compile + lint + test)
disable-model-invocation: true
allowed-tools: Bash, Read
argument-hint: [api|mobile]
---

Run full validation pipeline for the project.

## Steps

1. Determine target module from `$ARGUMENTS` (default: `api`)

### For API (`api`):

2. **Compile check:**
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew compileKotlin
   ```

3. **Auto-format lint issues:**
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintFormat
   ```

4. **Verify lint passes:**
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintCheck
   ```

5. **Tests:**
   ```
   cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test
   ```

### For Mobile (`mobile`):

2. **Compile check:**
   ```
   cd mobile && ./gradlew compileKotlin
   ```

3. **Lint check:**
   ```
   cd mobile && ./gradlew ktlintCheck
   ```

4. **Tests:**
   ```
   cd mobile && ./gradlew test
   ```

## Reporting

6. For each step, report:
   - PASS or FAIL
   - If FAIL: show the error, identify the root cause, suggest a fix
7. If all pass, report: **Validation passed** (compile, lint, tests)
8. If any fail:
   - Ask the user if you should fix the issues
   - If approved, fix and re-run the failed step
   - Repeat until all pass or user decides to stop
