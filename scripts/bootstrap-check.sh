#!/usr/bin/env bash
set -euo pipefail

./scripts/docs-presence-check.sh

required_paths=(
  "settings.gradle.kts"
  "build.gradle.kts"
  "app/build.gradle.kts"
  "app/src/main/AndroidManifest.xml"
  "app/src/main/java/com/angelmirror/MainActivity.kt"
)

for path in "${required_paths[@]}"; do
  if [[ ! -e "$path" ]]; then
    echo "Missing bootstrap path: $path" >&2
    exit 1
  fi
done

echo "Bootstrap structure is present."
