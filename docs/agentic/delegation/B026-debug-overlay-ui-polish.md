# B026 Debug Overlay UI Polish

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent. It should stay within existing Compose UI code. No ARCore, SceneView, or placement logic changes.

## Context

The debug overlay currently surfaces basic state information (tracking state, placement offsets, animation intent). The UI could be polished for better readability without changing what data is shown or how it's sourced.

## Goal

Improve the visual presentation of the debug overlay without changing placement, AR lifecycle, or data sources.

After the task:

- Debug overlay has improved spacing, typography, or color contrast.
- Layout is more readable on small screens.
- No new data sources or state logic are added.
- No changes to placement or AR session behavior.

## Non Goals

Do not:

- Change what data is displayed.
- Add new debug controls or toggles.
- Modify placement logic or AR lifecycle.
- Add animations or transitions.
- Change the debug overlay visibility logic.
- Add dependencies.

## Likely Files

- `app/src/main/java/com/angelmirror/ui/DebugOverlay.kt`
- `app/src/main/java/com/angelmirror/ui/theme/Theme.kt` (if color/typography changes needed)
- `docs/backlog.md`

## Suggested Implementation

Focus on Compose layout improvements:

- Use consistent spacing with `Spacer` or padding.
- Group related information with a simple `Column`/`Box` layout.
- Improve text hierarchy with `TextStyle` or font sizes.
- Ensure sufficient contrast for readability.

Example structure:

```kotlin
Column(modifier = Modifier.padding(8.dp)) {
    Text("Tracking: $state", style = MaterialTheme.typography.bodySmall)
    Text("Placement: $offset", style = MaterialTheme.typography.bodySmall)
}
```

## Validation

Run:

```sh
./scripts/docs-presence-check.sh
./scripts/format-check.sh
./gradlew lint
```

Run `./scripts/preflight.sh` if the local Android toolchain is available.

## Done Criteria

- Debug overlay compiles and renders without errors.
- No new dependencies added.
- Placement and AR behavior unchanged.
- B026 is moved to Done in `docs/backlog.md`.
- Final handoff states no functional behavior was changed.
