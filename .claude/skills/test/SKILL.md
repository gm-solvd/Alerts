---
name: test
description: Run the test suite and report results
disable-model-invocation: true
allowed-tools: Bash, Read
argument-hint: [api|mobile|specific test class]
---

Run tests and report results.

> `./gradlew test` runs **all tests**: unit tests and integration tests (Testcontainers PostgreSQL).
> Integration tests live in `api/src/test/kotlin/com/privacyalert/integration/` and require Docker running.

## Steps

1. Determine scope from `$ARGUMENTS`:
   - No arguments or `api`: `cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test`
   - `mobile`: `cd mobile && ./gradlew test`
   - Integration tests only: `cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test --tests "com.privacyalert.integration.*"`
   - Specific class: `cd api && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test --tests "$ARGUMENTS"`

2. Run the tests

3. Report:
   - Total tests run, passed, failed, skipped
   - Break down: unit tests vs integration tests
   - For each failure: test name, assertion message, file location

4. If tests fail:
   - Read the failing test file to understand intent
   - Read the implementation being tested
   - Suggest a fix (do NOT apply without approval)
