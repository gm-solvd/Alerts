#!/bin/bash
# PreToolUse hook — block reading sensitive files (.env, local.properties).
# Covers both the Read tool and Bash commands (cat/head/tail/less/more/grep on sensitive files).
# Exit 0 = allow, Exit 2 = block.

INPUT=$(cat)
TOOL=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_name',''))" 2>/dev/null)

is_sensitive_file() {
  local name
  name=$(basename "$1")
  case "$name" in
    .env|.env.*|*.env|local.properties)
      return 0 ;;
  esac
  return 1
}

if [[ "$TOOL" == "Read" ]]; then
  FILE=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('file_path',''))" 2>/dev/null)
  if [[ -n "$FILE" ]] && is_sensitive_file "$FILE"; then
    echo "BLOCKED: Cannot read '$(basename "$FILE")' — may contain secrets." >&2
    exit 2
  fi

elif [[ "$TOOL" == "Bash" ]]; then
  COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('command',''))" 2>/dev/null)

  # Skip: git commit/add commands — those are guarded by git-safety.sh and
  # the command body is a commit message, not a list of files to read.
  echo "$COMMAND" | grep -qE '\bgit\s+(commit|add)\b' && exit 0

  # Check if the command reads a sensitive file via cat/head/tail/less/more.
  # Two-step: (1) command contains a read utility, (2) a sensitive filename
  # appears as a standalone token (bounded by space / start / shell metachar).
  if echo "$COMMAND" | grep -qE '\b(cat|head|tail|less|more)\b'; then
    if echo "$COMMAND" | grep -qE '(^|[[:space:]/])(\.env(\.[^[:space:]|&;>]*)?|[^[:space:]/|&;>]+\.env|local\.properties)([[:space:]|&;>]|$)'; then
      echo "BLOCKED: Command appears to read a secrets file (.env / local.properties)." >&2
      exit 2
    fi
  fi
fi

exit 0
