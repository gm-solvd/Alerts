---
name: test
description: Run the test suite and report results
disable-model-invocation: true
allowed-tools: Bash, Read
argument-hint: [api|mobile|specific test class]
---

Run tests and report results.

## Steps

1. Determine scope from `$ARGUMENTS`:
   - No arguments or `api`: `cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test`
   - `mobile`: `cd mobile && ./gradlew test`
   - Specific class: `cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test --tests "$ARGUMENTS"`

2. Run the tests

3. Report:
   - Total tests run, passed, failed, skipped
   - For each failure: test name, assertion message, file location

4. If tests fail:
   - Read the failing test file to understand intent
   - Read the implementation being tested
   - Suggest a fix (do NOT apply without approval)
