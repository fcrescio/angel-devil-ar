#!/usr/bin/env bash
set -euo pipefail

# B020: Install latest CI debug APK from main branch

REPO_FULL_NAME="${REPO_FULL_NAME:-fcrescio/angel-devil-ar}"
ADB_HOST="${ADB_HOST:-host.docker.internal}"
ADB_PORT="${ADB_PORT:-5037}"
GH_BIN="${GH_BIN:-/home/dev/.local/bin/gh}"
ADB_BIN="${ADB_BIN:-/home/dev/.local/android-sdk/platform-tools/adb}"

ARTIFACT_NAME_PREFIX="angel-mirror-debug-"
WORKFLOW_NAME="Android CI"
BRANCH="main"
TMP_DIR="/tmp/angel-mirror-apk"

echo "Fetching GitHub token..."
export GH_TOKEN=$(printf 'protocol=https\nhost=github.com\n\n' | git credential fill | sed -n 's/^password=//p')

if [[ -z "$GH_TOKEN" ]]; then
    echo "ERROR: Failed to retrieve GitHub token. Please ensure git credential helper is configured."
    exit 1
fi

echo "Finding latest successful '${WORKFLOW_NAME}' run on '${BRANCH}'..."
RUN_ID=$("$GH_BIN" run list \
    --repo "$REPO_FULL_NAME" \
    --workflow "$WORKFLOW_NAME" \
    --branch "$BRANCH" \
    --status success \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId')

if [[ -z "$RUN_ID" || "$RUN_ID" == "null" ]]; then
    echo "ERROR: No successful '${WORKFLOW_NAME}' run found on '${BRANCH}'."
    exit 1
fi

echo "Latest successful run ID: ${RUN_ID}"

echo "Cleaning temp directory: ${TMP_DIR}"
rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR"

echo "Downloading APK artifacts..."
"$GH_BIN" run download "$RUN_ID" \
    --repo "$REPO_FULL_NAME" \
    --dir "$TMP_DIR"

APK_PATH=$(find "$TMP_DIR" -name 'app-debug.apk' -type f | head -1)

if [[ -z "$APK_PATH" || ! -f "$APK_PATH" ]]; then
    echo "ERROR: APK not found under ${TMP_DIR}"
    find "$TMP_DIR" -maxdepth 3 -type f -print || true
    exit 1
fi

ARTIFACT_DIR=$(basename "$(dirname "$APK_PATH")")
if [[ "$ARTIFACT_DIR" != "$ARTIFACT_NAME_PREFIX"* ]]; then
    echo "WARNING: artifact directory '${ARTIFACT_DIR}' does not start with '${ARTIFACT_NAME_PREFIX}'." >&2
fi

echo "Selected artifact: ${ARTIFACT_DIR}"
echo "APK path: ${APK_PATH}"

echo "Installing APK to device at ${ADB_HOST}:${ADB_PORT}..."
"$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" install -r "$APK_PATH"

INSTALLED_VERSION=$("$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" shell dumpsys package com.angelmirror |
    sed -n 's/.*versionName=//p' |
    head -1 |
    tr -d '\r')

if [[ -n "$INSTALLED_VERSION" ]]; then
    echo "Installed versionName: ${INSTALLED_VERSION}"
fi

echo "Installation complete."
