---
name: docs
description: Update SUMMARY.md files for changed directories and refresh README.md documentation links
allowed-tools: Bash, Read, Edit, Write, Glob, Grep
---

Keep documentation in sync after code changes. Update the `SUMMARY.md` for every directory that has changed files, then update `README.md`.

## Steps

1. **Identify changed directories**
   ```bash
   git diff --name-only HEAD
   git status --short
   ```
   Collect the set of unique directory paths that contain changed files.

2. **For each changed directory**, read the current `SUMMARY.md` (if it exists) and all source files in that directory, then rewrite the `SUMMARY.md` to reflect the current state:
   - List all files with accurate descriptions
   - Update function signatures, endpoints, or business logic if changed
   - Keep the existing format and headings

3. **Walk up the directory tree** — if a subdirectory's `SUMMARY.md` changed, also update its parent `SUMMARY.md` to keep links and descriptions accurate.

4. **Update `README.md`** — read the current README and refresh:
   - Ensure all documentation links are present and point to valid files
   - Update the "Documentation" section if new SUMMARY.md files were added

5. **Report** — list every file that was updated.

## SUMMARY.md Format

```markdown
# <Directory Name>

<One-sentence purpose of this directory.>

## Files

| File | Description |
|------|-------------|
| `Foo.kt` | What Foo does — key functions and business logic |
...

## (Optional sections: relationships, conventions, algorithms)
```

## Rules

- Do NOT commit — leave files staged or unstaged for the user to include in the next commit
- Do NOT change files that are not SUMMARY.md or README.md
- If a SUMMARY.md does not yet exist for a changed directory, create one
- Mirror the file/function descriptions from the actual current source code — do not guess
