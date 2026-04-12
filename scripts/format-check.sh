#!/usr/bin/env bash
set -euo pipefail

bad_files="$(find . \
  -path ./.git -prune -o \
  -path ./.gradle -prune -o \
  -type f \
  \( -name "*.kt" -o -name "*.kts" -o -name "*.md" -o -name "*.yml" -o -name "*.yaml" -o -name "*.xml" -o -name "*.sh" \) \
  -exec awk 'BEGIN { bad=0 } /[[:blank:]]$/ { print FILENAME ":" FNR ": trailing whitespace"; bad=1 } END { exit bad }' {} \; \
  2>&1 || true)"

if [[ -n "$bad_files" ]]; then
  echo "$bad_files" >&2
  exit 1
fi

echo "No trailing whitespace found."
