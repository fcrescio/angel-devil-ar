# B025 AR Diagnostic Log Model

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent. It should stay in pure Kotlin data classes and debug-only utilities. No ARCore, SceneView, or runtime behavior changes.

## Context

The app currently has minimal structured logging for AR events. Debugging placement drift, tracking loss, or animation state issues requires ad-hoc print statements. A lightweight, debug-only log model would help capture structured diagnostic events without adding production dependencies.

## Goal

Add a pure Kotlin diagnostic log model for AR events that is debug-only and has zero impact on release builds.

After the task:

- A sealed `ARDiagnosticEvent` hierarchy exists with common event types (tracking state, placement updates, animation intent changes).
- A simple `ARDiagnosticLogger` class collects events in memory for debug builds.
- Events are only emitted in debug builds (use `BuildConfig.DEBUG` or similar).
- No external logging dependencies are added.

## Non Goals

Do not:

- Add production logging or analytics.
- Integrate with ARCore or SceneView directly.
- Persist logs to disk.
- Add network or remote logging.
- Change existing AR or tracking behavior.
- Add UI for viewing logs.

## Likely Files

- `app/src/main/java/com/angelmirror/util/ARDiagnosticEvent.kt`
- `app/src/main/java/com/angelmirror/util/ARDiagnosticLogger.kt`
- `docs/backlog.md`

## Suggested Implementation

Define a sealed event hierarchy:

```kotlin
sealed class ARDiagnosticEvent(val timestampMs: Long = System.currentTimeMillis()) {
    data class TrackingState(val state: String, val faceCount: Int) : ARDiagnosticEvent()
    data class PlacementUpdate(val horizontal: Float, val vertical: Float, val depth: Float) : ARDiagnosticEvent()
    data class AnimationIntent(val intent: String) : ARDiagnosticEvent()
}
```

Implement a simple in-memory logger:

```kotlin
class ARDiagnosticLogger {
    private val events = mutableListOf<ARDiagnosticEvent>()

    fun log(event: ARDiagnosticEvent) {
        if (!BuildConfig.DEBUG) return
        events.add(event)
    }

    fun getEvents(): List<ARDiagnosticEvent> = events.toList()
    fun clear() { events.clear() }
}
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

- Pure Kotlin data classes compile.
- No new dependencies added.
- Debug-only guard is in place.
- B025 is moved to Done in `docs/backlog.md`.
- Final handoff states no runtime behavior was changed.
