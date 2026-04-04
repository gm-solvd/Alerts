#!/bin/bash
# Consolidated PreToolUse hook — hard enforcement of git safety rules.
# Exit 0 = allow, Exit 2 = block (with stderr message shown to Claude).

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('command',''))" 2>/dev/null)

# --- Block: git add . / -A / --all ---
if echo "$COMMAND" | grep -qE '\bgit\s+add\s+(-A|--all)\b' || echo "$COMMAND" | grep -qE '\bgit\s+add\s+\.\s*($|[;&|])'; then
  echo "BLOCKED: Stage files individually by name. Do not use 'git add .', 'git add -A', or 'git add --all'." >&2
  exit 2
fi

# --- Block: git push --force / -f ---
if echo "$COMMAND" | grep -qE '\bgit\s+push\b'; then
  if echo "$COMMAND" | grep -qE '(\s--force\b|\s-f\b)'; then
    echo "BLOCKED: Force push is not allowed. Resolve conflicts with rebase and normal push." >&2
    exit 2
  fi
fi

BRANCH=$(git branch --show-current 2>/dev/null)

# --- Block: git commit on protected branch ---
if echo "$COMMAND" | grep -qE '\bgit\s+commit\b'; then
  if [[ "$BRANCH" == "main" || "$BRANCH" == "develop" ]]; then
    echo "BLOCKED: Cannot commit directly to '$BRANCH'. Create a feature/fix branch first." >&2
    exit 2
  fi

  # --- Block: secrets in staged files ---
  STAGED=$(git diff --cached --name-only 2>/dev/null)
  if [[ -n "$STAGED" ]]; then
    VIOLATIONS=""
    while IFS= read -r file; do
      [[ -z "$file" ]] && continue
      case "$file" in
        .env|*.env|*.env.*) VIOLATIONS="${VIOLATIONS}  - ${file} (environment file)\n" ;;
        *credentials*|*secret*|*.jks|*.p12|*.pem|*.key) VIOLATIONS="${VIOLATIONS}  - ${file} (potential credentials)\n" ;;
        local.properties) VIOLATIONS="${VIOLATIONS}  - ${file} (local config)\n" ;;
      esac
    done <<< "$STAGED"

    if [[ -n "$VIOLATIONS" ]]; then
      printf "BLOCKED: Staged files may contain secrets:\n%bUnstage these files or verify they are safe." "$VIOLATIONS" >&2
      exit 2
    fi
  fi
fi

# --- Block: git push to protected branch ---
if echo "$COMMAND" | grep -qE '\bgit\s+push\b'; then
  # Explicit target: git push origin main/develop
  if echo "$COMMAND" | grep -qE '\bgit\s+push\s+\S+\s+(main|develop)\b'; then
    echo "BLOCKED: Cannot push directly to main or develop. Use a feature branch and create a PR." >&2
    exit 2
  fi
  # Implicit: on protected branch and pushing (git push, git push -u origin HEAD)
  if [[ "$BRANCH" == "main" || "$BRANCH" == "develop" ]]; then
    echo "BLOCKED: Currently on '$BRANCH'. Cannot push to protected branches. Switch to a feature branch." >&2
    exit 2
  fi
fi

exit 0
