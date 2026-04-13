# B027 Asset Validation and Documentation Check

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent. It should stay in shell scripts and documentation. No Android/Kotlin code or GLB asset changes.

## Context

The app now has an app-owned GLB placeholder asset (grotesque imp). A lightweight validation check would ensure assets meet basic requirements (file exists, valid glTF/GLB structure, reasonable size) and that documentation is in sync.

## Goal

Add a shell-based asset validation check and ensure asset documentation is complete.

After the task:

- A shell script validates app-owned GLB assets exist and have valid structure.
- Asset documentation lists all app-owned assets with their purpose and constraints.
- No Android/Kotlin code is changed.
- No GLB assets are modified.

## Non Goals

Do not:

- Modify GLB assets.
- Add Android/Kotlin code.
- Add dependencies to the app.
- Change the asset loading pipeline.
- Add runtime validation.

## Likely Files

- `scripts/validate-assets.sh`
- `docs/assets.md` (or update existing asset documentation)
- `docs/backlog.md`

## Suggested Implementation

Create a shell script that checks:

```sh
#!/usr/bin/env bash
set -euo pipefail

asset_dir="app/src/main/assets"

# Check app-owned imp GLB exists
if [[ ! -f "$asset_dir/models/grotesque_imp.glb" ]]; then
    echo "Missing: $asset_dir/models/grotesque_imp.glb" >&2
    exit 1
fi

# Check file size is reasonable (< 5MB)
size=$(stat -f%z "$asset_dir/models/grotesque_imp.glb" 2>/dev/null || stat -c%s "$asset_dir/models/grotesque_imp.glb" 2>/dev/null)
if [[ $size -gt 5242880 ]]; then
    echo "Asset too large: $size bytes" >&2
    exit 1
fi

echo "Asset validation passed."
```

Update documentation to list:

- Asset filename and location
- Purpose (placeholder character)
- Size constraints
- License/attribution if applicable

## Validation

Run:

```sh
./scripts/docs-presence-check.sh
./scripts/format-check.sh
./scripts/validate-assets.sh
```

Run `./scripts/preflight.sh` if the local Android toolchain is available.

## Done Criteria

- Shell script runs without errors.
- Asset documentation is updated.
- No Android/Kotlin code changed.
- B027 is moved to Done in `docs/backlog.md`.
- Final handoff states no runtime behavior was changed.
