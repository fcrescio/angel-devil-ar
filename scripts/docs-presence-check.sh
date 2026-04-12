#!/usr/bin/env bash
set -euo pipefail

required_files=(
  "README.md"
  "AGENTS.md"
  "docs/project-brief.md"
  "docs/architecture.md"
  "docs/roadmap.md"
  "docs/backlog.md"
  "docs/decisions/ADR-0001-stack.md"
  "docs/decisions/ADR-0002-repo-structure.md"
  "docs/decisions/ADR-0003-agentic-workflow.md"
  "docs/agentic/task-template.md"
  "docs/agentic/done-criteria.md"
  "docs/agentic/coding-constraints.md"
  "docs/agentic/session-handoff-template.md"
)

for file in "${required_files[@]}"; do
  if [[ ! -s "$file" ]]; then
    echo "Missing required doc: $file" >&2
    exit 1
  fi
done

echo "Required docs are present."
