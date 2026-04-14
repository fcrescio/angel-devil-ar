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

if ! command -v readelf >/dev/null 2>&1; then
  echo "readelf is not available; skipped ELF segment alignment check." >&2
  exit 0
fi

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

if ! unzip -qq "$apk_path" 'lib/*/*.so' -d "$tmp_dir" 2>/dev/null; then
  echo "No native libraries found; skipped ELF segment alignment check."
  exit 0
fi

failed=0
while IFS= read -r so_file; do
  while IFS= read -r alignment; do
    if [[ "$alignment" != "0x4000" ]]; then
      echo "ELF segment alignment is not 16 KB: ${so_file#"$tmp_dir/"} has $alignment" >&2
      failed=1
    fi
  done < <(readelf -W -l "$so_file" | awk '/LOAD/ {print $NF}' | sort -u)
done < <(find "$tmp_dir/lib" -name '*.so' -type f | sort)

if [[ "$failed" -ne 0 ]]; then
  echo "Native libraries must use 16 KB ELF LOAD segment alignment." >&2
  exit 1
fi

echo "Native libraries pass 16 KB ELF segment alignment check."
