# B022 Placement Smoothing Tests

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent only if it stays in pure Kotlin tests. It should not touch ARCore, SceneView, or device behavior.

## Context

The current validated placement profile is `pixel7-ear-shoulder`:

- horizontal offset: `0.15m`
- vertical offset: `-0.01m`
- depth offset: `-0.08m`
- placeholder scale: `0.18`

The user has explicitly approved this placement. Do not change it.

`ShoulderPlacementSolver` is pure Kotlin and already has one unit test. `FaceRelativeCharacterController` has smoothing internally, but it depends on SceneView and ARCore types, so this task should avoid testing that class directly unless the code is first refactored carefully.

## Goal

Increase pure Kotlin test coverage around placement math without changing runtime placement behavior.

After the task:

- Tests verify that `CharacterPlacementProfiles.Default` remains the validated Pixel 7 profile.
- Tests verify that `ShoulderPlacementSolver` applies positive/negative/zero offsets correctly.
- Tests verify that the default profile values do not drift.
- If needed, extract a tiny pure Kotlin smoothing helper, but only if it does not alter observed runtime behavior.

## Non Goals

Do not:

- Change `CharacterPlacementProfiles.Pixel7EarShoulder` values.
- Change `FaceRelativeCharacterController` behavior unless extracting identical math.
- Add ARCore test doubles.
- Add SceneView tests.
- Add instrumentation tests.
- Add dependencies.
- Modify UI.

## Likely Files

- `app/src/test/java/com/angelmirror/BootstrapStatusTest.kt`
- `app/src/main/java/com/angelmirror/character/ShoulderPlacement.kt`
- `app/src/main/java/com/angelmirror/character/CharacterPlacementProfile.kt`
- `docs/backlog.md`

## Suggested Implementation

Prefer adding focused tests to the existing test class:

- `defaultPlacementProfileKeepsValidatedPixel7Offsets`
- `shoulderPlacementHandlesZeroOffset`
- `shoulderPlacementHandlesNegativeHorizontalOffset`

Use exact values with a small float delta:

```kotlin
assertEquals(0.15f, offset.horizontalMeters, 0.001f)
assertEquals(-0.01f, offset.verticalMeters, 0.001f)
assertEquals(-0.08f, offset.depthMeters, 0.001f)
assertEquals(0.18f, CharacterPlacementProfiles.Default.scaleToUnits, 0.001f)
```

## Validation

Run:

```sh
./scripts/docs-presence-check.sh
./scripts/format-check.sh
./gradlew test
```

Run `./scripts/preflight.sh` if the local Android toolchain is available.

## Done Criteria

- New tests pass.
- Runtime placement values are unchanged.
- No new dependencies.
- B022 is moved to Done in `docs/backlog.md`.
- Final handoff explicitly states that placement values were not changed.
