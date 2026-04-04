#!/bin/bash
# Notification hook: macOS notification when Claude needs attention.
osascript -e 'display notification "Waiting for your input." with title "Claude Code" subtitle "Attention Needed" sound name "Submarine"' 2>/dev/null
exit 0
