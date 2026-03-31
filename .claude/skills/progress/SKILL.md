---
name: progress
description: Update the MVP progress tracking document
disable-model-invocation: true
allowed-tools: Bash, Read, Edit
argument-hint: [phase number and status]
---

Update the progress tracker at `rules/general/progress.md`.

## Steps

1. Read `rules/general/progress.md`
2. Parse `$ARGUMENTS` to determine what to update:
   - Example: `api 4 done` → API phase 4, mark as DONE
   - Example: `api 5 in progress` → API phase 5, mark as IN PROGRESS
   - Example: `mobile 1 done` → Mobile phase 1, mark as DONE
3. Update the status column for the specified phase
4. Set the date to today's date
5. Add any notes if provided
6. Show the updated table
7. **Do NOT commit** — leave for the user to include in the next commit

## Valid statuses
- `-` (not started)
- `IN PROGRESS`
- `DONE`
- `BLOCKED`
