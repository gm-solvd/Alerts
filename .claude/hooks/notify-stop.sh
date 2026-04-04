#!/bin/bash
# Stop hook: macOS notification when Claude finishes responding.
osascript -e 'display notification "Ready for your input." with title "Claude Code" subtitle "Task Complete" sound name "Glass"' 2>/dev/null
exit 0
