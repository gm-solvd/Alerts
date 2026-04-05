#!/bin/bash
# PreToolUse hook — block gh pr merge unless full validation has passed.
# Uses marker-file approach: /tmp/.claude-validation-passed-{platform}-{HEAD_SHA_8}
# SHA-pinning ensures any new commit invalidates prior validation.
# Exit 0 = allow, Exit 2 = block.

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('command',''))" 2>/dev/null)

# Only intercept gh pr merge commands (both short and full path)
if ! echo "$COMMAND" | grep -qE '(^|\s|/|;|&&|\|)(/opt/homebrew/bin/)?gh\s+pr\s+merge\b'; then
  exit 0
fi

# Extract PR number from command (e.g., "gh pr merge 54 --squash")
PR_NUMBER=$(echo "$COMMAND" | grep -oE 'merge\s+[0-9]+' | grep -oE '[0-9]+')

# Fallback: get PR for current branch
if [ -z "$PR_NUMBER" ]; then
  PR_NUMBER=$(/opt/homebrew/bin/gh pr view --json number -q '.number' 2>/dev/null)
fi

if [ -z "$PR_NUMBER" ]; then
  echo "BLOCKED: Could not determine PR number for merge validation." >&2
  exit 2
fi

# Get changed files from the PR
CHANGED=$(/opt/homebrew/bin/gh pr diff "$PR_NUMBER" --name-only 2>/dev/null)

if [ -z "$CHANGED" ]; then
  # Fallback to git diff against base branch
  BASE=$(/opt/homebrew/bin/gh pr view "$PR_NUMBER" --json baseRefName -q '.baseRefName' 2>/dev/null)
  [ -z "$BASE" ] && BASE="develop"
  CHANGED=$(git diff --name-only "origin/${BASE}...HEAD" 2>/dev/null)
fi

# Detect which platforms are affected
NEEDS_API=false
NEEDS_MOBILE=false
CONFIG_ONLY=true

while IFS= read -r f; do
  [ -z "$f" ] && continue
  case "$f" in
    api/*)    NEEDS_API=true; CONFIG_ONLY=false ;;
    mobile/*) NEEDS_MOBILE=true; CONFIG_ONLY=false ;;
    .claude/*|rules/*|CLAUDE.md|README.md|.github/*) ;;
    *)        CONFIG_ONLY=false ;;
  esac
done <<< "$CHANGED"

# Config-only changes skip validation
if [ "$CONFIG_ONLY" = "true" ]; then
  exit 0
fi

# Get current HEAD SHA for marker check
HEAD_SHA=$(git rev-parse --short=8 HEAD 2>/dev/null)
if [ -z "$HEAD_SHA" ]; then
  echo "BLOCKED: Cannot determine HEAD SHA for validation check." >&2
  exit 2
fi

# Check for platform-specific validation markers
MISSING=""

if [ "$NEEDS_API" = "true" ] && [ ! -f "/tmp/.claude-validation-passed-api-${HEAD_SHA}" ]; then
  MISSING="${MISSING}  - API validation missing (run: /validate api)\n"
fi

if [ "$NEEDS_MOBILE" = "true" ] && [ ! -f "/tmp/.claude-validation-passed-mobile-${HEAD_SHA}" ]; then
  MISSING="${MISSING}  - Mobile validation missing (run: /validate mobile)\n"
fi

if [ -n "$MISSING" ]; then
  printf "BLOCKED: Cannot merge PR #%s — full validation has not passed for HEAD %s.\n" "$PR_NUMBER" "$HEAD_SHA" >&2
  printf "Missing:\n%b" "$MISSING" >&2
  printf "\nRun full validation first. The marker file is created automatically on success.\n" >&2
  exit 2
fi

exit 0
