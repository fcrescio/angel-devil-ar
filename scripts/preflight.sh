#!/usr/bin/env bash
set -euo pipefail

./scripts/bootstrap-check.sh
./scripts/format-check.sh
./scripts/dependency-validation-check.sh
./scripts/validate-assets.sh

if [[ -x "./gradlew" ]] && command -v java >/dev/null 2>&1; then
  ./gradlew assembleDebug
  ./gradlew test
  ./gradlew lint
else
  echo "Gradle or Java is not available; skipped Android build/test/lint." >&2
fi
