#!/usr/bin/env bash
set -euo pipefail

apk_path="${1:-app/build/outputs/apk/debug/app-debug.apk}"

if [[ ! -f "$apk_path" ]]; then
  echo "APK not found: $apk_path" >&2
  exit 1
fi

if ! command -v zipalign >/dev/null 2>&1; then
  echo "zipalign is not available; skipped 16 KB APK alignment check." >&2
  exit 0
fi

if zipalign -c -P 16 -v 4 "$apk_path"; then
  echo "APK passes zipalign 16 KB alignment check."
else
  echo "APK did not pass zipalign 16 KB alignment check." >&2
  echo "Keeping this non-blocking for debug builds; update native dependencies before release." >&2
fi
