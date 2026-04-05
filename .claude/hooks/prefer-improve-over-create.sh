#!/bin/bash
# PreToolUse hook — block new source file creation on first attempt.
# Forces a "check improvement route first" pause via marker-file mechanism.
# Second attempt (marker exists) is allowed automatically.
# Exit 0 = allow, Exit 2 = block (with stderr message shown to Claude).

INPUT=$(cat)
TOOL=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_name',''))" 2>/dev/null)

# Only applies to the Write tool
[[ "$TOOL" != "Write" ]] && exit 0

FILE_PATH=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('file_path',''))" 2>/dev/null)
[[ -z "$FILE_PATH" ]] && exit 0

# --- Allow: file already exists (overwrite/update, not creation) ---
[[ -f "$FILE_PATH" ]] && exit 0

# --- Allow: allowlisted paths (configs, docs, tests, resources) ---
BASENAME=$(basename "$FILE_PATH")
case "$FILE_PATH" in
  */.claude/*|*/rules/*|*/.github/*) exit 0 ;;
  */resources/*|*/gradle/*) exit 0 ;;
  */build.gradle*|*/settings.gradle*) exit 0 ;;
esac

case "$BASENAME" in
  *.md|*.json|*.yml|*.yaml|*.xml|*.sql|*.properties|*.toml) exit 0 ;;
esac

# Allow test files
case "$FILE_PATH" in
  *Test.kt|*Test.java|*Test.swift|*Tests.swift) exit 0 ;;
  */test/*|*/androidTest/*|*/iosTest/*|*/commonTest/*|*/jvmTest/*) exit 0 ;;
esac

# --- Source file check: only block .kt/.swift/.ts/.vue/.java in project source dirs ---
IS_SOURCE=false
case "$BASENAME" in
  *.kt|*.java|*.swift|*.ts|*.tsx|*.vue) IS_SOURCE=true ;;
esac

if [[ "$IS_SOURCE" != "true" ]]; then
  exit 0
fi

# Check if file is in a project source directory (handles both absolute and relative paths)
case "$FILE_PATH" in
  */api/src/*|api/src/*|*/mobile/*|mobile/*) ;; # Continue to marker check
  *) exit 0 ;; # Not in project source dirs, allow
esac

# --- Marker-file mechanism: block first attempt, allow second ---
HASH=$(echo -n "$FILE_PATH" | md5 2>/dev/null || echo -n "$FILE_PATH" | md5sum 2>/dev/null | cut -d' ' -f1)
MARKER="/tmp/.claude-write-ack-${HASH}"

if [[ -f "$MARKER" ]]; then
  rm -f "$MARKER"
  exit 0
fi

# First attempt — create marker and block
touch "$MARKER"
cat >&2 <<'MSG'
BLOCKED: New source file detected. Before creating, verify:
  1. Can this functionality be added to an existing file in the same package?
  2. Can an existing function/class be extended instead?
  3. Is this file architecturally justified (new entity, use case, controller, etc.)?
If still needed, retry the same Write call — it will be allowed on second attempt.
MSG
exit 2
