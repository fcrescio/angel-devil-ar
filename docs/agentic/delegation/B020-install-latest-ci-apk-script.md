# B020 Install Latest CI APK Script

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent. It is shell scripting plus docs only. It should not require Android/Kotlin code changes.

## Context

The project already uploads a debug APK from GitHub Actions as artifact `angel-mirror-debug-apk`. The local environment can install APKs on the Pixel device through an adb server reachable at `host.docker.internal:5037`.

Current manual flow:

```sh
token=$(printf 'protocol=https\nhost=github.com\n\n' | git credential fill | sed -n 's/^password=//p')
GH_TOKEN="$token" /home/dev/.local/bin/gh run download <run-id> --repo fcrescio/angel-devil-ar --name angel-mirror-debug-apk --dir /tmp/angel-mirror-apk
/home/dev/.local/android-sdk/platform-tools/adb -H host.docker.internal -P 5037 install -r /tmp/angel-mirror-apk/app-debug.apk
```

## Goal

Add a script that installs the latest successful CI debug APK from `main` onto the connected device.

After the task:

- A script exists at `scripts/install-latest-ci-apk.sh`.
- It finds the latest successful `Android CI` run on `main`.
- It downloads artifact `angel-mirror-debug-apk`.
- It installs `app-debug.apk` using adb.
- It accepts optional env overrides for:
  - `REPO_FULL_NAME`, default `fcrescio/angel-devil-ar`
  - `ADB_HOST`, default `host.docker.internal`
  - `ADB_PORT`, default `5037`
  - `GH_BIN`, default `/home/dev/.local/bin/gh`
  - `ADB_BIN`, default `/home/dev/.local/android-sdk/platform-tools/adb`
- It never prints the GitHub token.
- `docs/debug-install.md` documents the new script.

## Non Goals

Do not change:

- Android app code.
- Gradle files.
- GitHub Actions.
- Signing config.
- AR placement values.

Do not add dependencies.

## Likely Files

- `scripts/install-latest-ci-apk.sh`
- `docs/debug-install.md`
- `docs/backlog.md`

## Suggested Implementation

Use `git credential fill` to read the GitHub token without echoing it. Use `gh run list` to get the latest successful `Android CI` run on `main`. Prefer `--json databaseId,name,status,conclusion,headBranch` and parse with `jq` only if `jq` is already available. If `jq` is not available, use a plain `gh run list --workflow "Android CI" --branch main --status success --limit 1` format that is easy to parse.

Use a temp directory under `/tmp`, remove any previous contents for this script, and fail clearly if the artifact or APK is missing.

## Validation

Run:

```sh
./scripts/docs-presence-check.sh
./scripts/format-check.sh
./scripts/install-latest-ci-apk.sh
```

If a device is unavailable, the script should fail clearly at the adb step. Include that output in the handoff.

## Done Criteria

- Script is executable.
- Script does not print credentials.
- Script installs the latest successful CI APK when a device is available.
- Docs mention the script and env overrides.
- B020 is moved to Done in `docs/backlog.md`.
