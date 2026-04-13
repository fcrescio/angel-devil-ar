# B021 Character Animation State Boundary

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent. It is pure Kotlin state modeling and tests. It should not require SceneView, ARCore, device access, or asset changes.

## Context

The app currently has a static placeholder fox model. Future work will need animation states such as idle, appear, searching, and blocked, but the current GLB asset may not expose useful animations. The right next step is to define app-level animation intent without controlling SceneView nodes yet.

Existing relevant packages:

- `character`: placement, GLB node creation, debug state.
- `interaction`: companion moods and cues.

## Goal

Add a pure Kotlin boundary for character animation intent.

After the task:

- A new file exists under `app/src/main/java/com/angelmirror/character/`.
- It defines a small enum or sealed interface for animation intent:
  - `Hidden` or `Unavailable`
  - `Appearing`
  - `Idle`
  - `Searching`
  - `Blocked`
  - `Paused`
- It includes a mapper from `CompanionMood` to character animation intent.
- Unit tests cover the mapper.
- No rendering code consumes this yet.

## Non Goals

Do not:

- Add or replace GLB assets.
- Inspect or require model animations.
- Modify `CharacterModelNodeFactory`.
- Modify `FaceRelativeCharacterController`.
- Add runtime dependencies.
- Change placement offsets or scale.
- Add Compose UI.
- Add ARCore/SceneView code.

## Likely Files

- `app/src/main/java/com/angelmirror/character/CharacterAnimationIntent.kt`
- `app/src/test/java/com/angelmirror/BootstrapStatusTest.kt`
- `docs/backlog.md`
- `docs/architecture.md` if needed

## Suggested Implementation

Keep it small:

```kotlin
enum class CharacterAnimationIntent {
    Appearing,
    Idle,
    Searching,
    Blocked,
    Paused,
}
```

Then add a mapper object or function:

```kotlin
object CharacterAnimationIntentMapper {
    fun fromMood(mood: CompanionMood): CharacterAnimationIntent = ...
}
```

Use explicit mapping:

- `WarmingUp -> Appearing`
- `Present -> Idle`
- `Searching -> Searching`
- `Blocked -> Blocked`
- `Paused -> Paused`

## Validation

Run:

```sh
./scripts/docs-presence-check.sh
./scripts/format-check.sh
./scripts/dependency-validation-check.sh
./gradlew test
```

Run `./gradlew assembleDebug` if Android SDK/JDK are available.

## Done Criteria

- Mapper is pure Kotlin and unit-tested.
- No rendering behavior changes.
- No new dependencies.
- B021 is moved to Done in `docs/backlog.md`.
- Final handoff lists validation commands.
