---
name: test
description: Run the test suite and report results
disable-model-invocation: true
allowed-tools: Bash, Read
---

Run tests and report results.

## Steps

1. Determine scope:
   - No arguments: run `./gradlew test`
   - With `$ARGUMENTS`: run scoped tests (e.g., `./gradlew :backend:test`, `./gradlew :mobile:shared:test`)
2. Run the tests
3. Report:
   - Total tests run, passed, failed, skipped
   - For each failure: test name, assertion message, file location
4. If tests fail:
   - Read the failing test file to understand intent
   - Read the implementation being tested
   - Suggest a fix (do NOT apply without approval)
