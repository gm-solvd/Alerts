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
   cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:compileDebugKotlinAndroid
   ```

3. **Detekt static analysis:**
   ```
   cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:detektMetadataCommonMain
   ```
   - If detekt fails, read the report, fix the issues, and re-run
   - Suppress only when the violation is intentional (e.g., `@Suppress("LongMethod")` on declarative builders)

4. **Unused code cleanup:**
   - Check detekt output for `UnusedImports`, `UnusedPrivateMember`, `UnusedParameter` warnings
   - Remove any unused imports, functions, classes, or parameters
   - Re-run detekt to confirm clean

5. **Tests:**
   ```
   cd mobile && export JAVA_HOME=/opt/homebrew/opt/openjdk@21 && export ANDROID_HOME=~/Library/Android/sdk && ./gradlew :shared:allTests
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

## Merge Marker

9. **Only when ALL steps passed**, create the validation marker so the pre-merge hook allows `gh pr merge`:
   ```bash
   touch "/tmp/.claude-validation-passed-api-$(git rev-parse --short=8 HEAD)"    # for API
   touch "/tmp/.claude-validation-passed-mobile-$(git rev-parse --short=8 HEAD)" # for Mobile
   ```
   - Use the platform matching `$ARGUMENTS` (api or mobile)
   - The marker is keyed to HEAD SHA — any new commit invalidates it automatically
   - Do NOT create the marker if any step failed
