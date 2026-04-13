# Debug Install

Debug builds use the versioned keystore at `app/keystores/debug.keystore`.

This key is intentionally not secret. It exists only to make CI artifacts installable over previous debug builds with:

```sh
adb install -r app-debug.apk
```

Release builds must never use this keystore.

## Install Latest CI APK

Install the latest successful debug APK from the `main` branch CI run:

```sh
./scripts/install-latest-ci-apk.sh
```

The script:

- Finds the latest successful `Android CI` run on `main`.
- Downloads the `angel-mirror-debug-apk` artifact.
- Installs `app-debug.apk` via adb.

### Environment Overrides

| Variable | Default | Description |
|----------|---------|-------------|
| `REPO_FULL_NAME` | `fcrescio/angel-devil-ar` | GitHub repository |
| `ADB_HOST` | `host.docker.internal` | ADB server host |
| `ADB_PORT` | `5037` | ADB server port |
| `GH_BIN` | `/home/dev/.local/bin/gh` | GitHub CLI path |
| `ADB_BIN` | `/home/dev/.local/android-sdk/platform-tools/adb` | ADB binary path |

Example:

```sh
ADB_HOST=192.168.1.100 ./scripts/install-latest-ci-apk.sh
```

## 16 KB Page Size Check

The Android CI runs `scripts/apk-alignment-check.sh` after `assembleDebug`. The script uses `zipalign -c -P 16` when `zipalign` is available and keeps the debug build non-blocking while native dependency compatibility is still being validated.
