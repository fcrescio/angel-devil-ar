#!/usr/bin/env bash
set -euo pipefail

gradle_file="app/build.gradle.kts"
expected_arcore_version="${EXPECTED_ARCORE_VERSION:-1.49.0}"
expected_sceneview_version="${EXPECTED_SCENEVIEW_VERSION:-2.3.2}"

if [[ ! -f "$gradle_file" ]]; then
  echo "Missing Gradle module file: $gradle_file" >&2
  exit 1
fi

require_dependency() {
  local coordinate="$1"
  local version="$2"

  if ! grep -Fq "implementation(\"$coordinate:$version\")" "$gradle_file"; then
    echo "Missing or unexpected dependency version: $coordinate:$version" >&2
    echo "Update this check only after validating the new version on a real ARCore device." >&2
    exit 1
  fi

  if grep -Eq "implementation\\(\"$coordinate:[^\"]*[+][^\"]*\"\\)" "$gradle_file"; then
    echo "Dynamic dependency versions are not allowed for $coordinate." >&2
    exit 1
  fi
}

require_dependency "com.google.ar:core" "$expected_arcore_version"
require_dependency "io.github.sceneview:arsceneview" "$expected_sceneview_version"

echo "ARCore and SceneView dependency versions are pinned and validated."
