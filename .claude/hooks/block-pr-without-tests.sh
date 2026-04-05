#!/bin/bash
# PreToolUse hook — block gh pr create unless tests pass for affected platforms.
# Uses same marker-file approach as block-merge-without-validation.sh:
#   /tmp/.claude-validation-passed-{platform}-{HEAD_SHA_8}
# Exit 0 = allow, Exit 2 = block.

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('command',''))" 2>/dev/null)

# Only intercept gh pr create commands
if ! echo "$COMMAND" | grep -qE '(^|\s|/|;|&&|\|)(/opt/homebrew/bin/)?gh\s+pr\s+create\b'; then
  exit 0
fi

# Determine base branch from --base flag, default to develop
BASE=$(echo "$COMMAND" | grep -oE '\-\-base\s+\S+' | awk '{print $2}')
[ -z "$BASE" ] && BASE="develop"

# Get changed files compared to base
CHANGED=$(git diff --name-only "origin/${BASE}...HEAD" 2>/dev/null)

if [ -z "$CHANGED" ]; then
  echo "BLOCKED: No changes detected against ${BASE}. Nothing to create a PR for." >&2
  exit 2
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
  MISSING="${MISSING}  - API tests not passed (run: /validate api)\n"
fi

if [ "$NEEDS_MOBILE" = "true" ] && [ ! -f "/tmp/.claude-validation-passed-mobile-${HEAD_SHA}" ]; then
  MISSING="${MISSING}  - Mobile tests not passed (run: /validate mobile)\n"
fi

if [ -n "$MISSING" ]; then
  printf "BLOCKED: Cannot create PR — tests have not passed for HEAD %s.\n" "$HEAD_SHA" >&2
  printf "Missing:\n%b" "$MISSING" >&2
  printf "\nRun tests and validation first. The marker file is created automatically on success.\n" >&2
  exit 2
fi

exit 0
