#!/usr/bin/env bash
set -euo pipefail

ADB_HOST="${ADB_HOST:-host.docker.internal}"
ADB_PORT="${ADB_PORT:-5037}"
ADB_BIN="${ADB_BIN:-/home/dev/.local/android-sdk/platform-tools/adb}"
APK_PATH="${APK_PATH:-app/build/outputs/apk/debug/app-debug.apk}"
PACKAGE_NAME="${PACKAGE_NAME:-com.angelmirror}"

EXPECTED_SHA="$(git rev-parse --short=7 HEAD)"

echo "Building debug APK for git ${EXPECTED_SHA}..."
./gradlew assembleDebug --rerun-tasks

if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found at ${APK_PATH}" >&2
  exit 1
fi

echo "Expected versionName suffix: ${EXPECTED_SHA}"
echo "APK path: ${APK_PATH}"

if [[ ! -x "$ADB_BIN" ]]; then
  echo "ERROR: adb not found or not executable at ${ADB_BIN}" >&2
  exit 1
fi

DEVICE_LIST="$("$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" devices | sed '1d' | awk '$2 == "device" {print $1}')"
if [[ -z "$DEVICE_LIST" ]]; then
  echo "ERROR: no adb device available at ${ADB_HOST}:${ADB_PORT}" >&2
  "$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" devices >&2 || true
  exit 1
fi

echo "Installing APK to adb device at ${ADB_HOST}:${ADB_PORT}..."
"$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" install -r "$APK_PATH"

INSTALLED_VERSION="$("$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" shell dumpsys package "$PACKAGE_NAME" |
  sed -n 's/.*versionName=//p' |
  head -1 |
  tr -d '\r')"

if [[ -z "$INSTALLED_VERSION" ]]; then
  echo "WARNING: installed versionName could not be read for ${PACKAGE_NAME}" >&2
else
  echo "Installed versionName: ${INSTALLED_VERSION}"
  if [[ "$INSTALLED_VERSION" != *"${EXPECTED_SHA}"* ]]; then
    echo "ERROR: installed versionName does not include expected git hash ${EXPECTED_SHA}" >&2
    exit 1
  fi
fi

echo "Installation complete."
