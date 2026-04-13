#!/usr/bin/env bash
set -euo pipefail

# B020: Install latest CI debug APK from main branch

REPO_FULL_NAME="${REPO_FULL_NAME:-fcrescio/angel-devil-ar}"
ADB_HOST="${ADB_HOST:-host.docker.internal}"
ADB_PORT="${ADB_PORT:-5037}"
GH_BIN="${GH_BIN:-/home/dev/.local/bin/gh}"
ADB_BIN="${ADB_BIN:-/home/dev/.local/android-sdk/platform-tools/adb}"

ARTIFACT_NAME="angel-mirror-debug-apk"
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

echo "Downloading artifact '${ARTIFACT_NAME}'..."
"$GH_BIN" run download "$RUN_ID" \
    --repo "$REPO_FULL_NAME" \
    --name "$ARTIFACT_NAME" \
    --dir "$TMP_DIR"

APK_PATH="$TMP_DIR/app-debug.apk"

if [[ ! -f "$APK_PATH" ]]; then
    echo "ERROR: APK not found at ${APK_PATH}"
    ls -la "$TMP_DIR" || true
    exit 1
fi

echo "Installing APK to device at ${ADB_HOST}:${ADB_PORT}..."
"$ADB_BIN" -H "$ADB_HOST" -P "$ADB_PORT" install -r "$APK_PATH"

echo "Installation complete."
