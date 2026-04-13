# B019 Interaction Cue Surface

## Intended Delegate

This task is suitable for a smaller/cheaper coding agent. It is intentionally narrow, mostly pure Kotlin, and should not require ARCore, SceneView, device testing, or architectural decisions.

## Context

Angel Mirror AR already has a working AR mirror flow:

- Front camera ARCore Augmented Faces session.
- Placeholder fox model placed near the validated shoulder/ear position.
- A top overlay that respects Android status/cutout insets.
- A debug toggle and bottom placement debug overlay.
- A new `interaction` package with deterministic state:
  - `CompanionInteractionState`
  - `CompanionCue`
  - `CompanionMood`
  - `CompanionInteractionReducer`

The current interaction layer is deliberately simple. It maps AR signals into one text cue and keeps `voiceInputEnabled` and `llmResponseEnabled` false.

## Goal

Make the companion cue surface a little more complete while keeping it deterministic and local.

After the task:

- The companion has distinct cue text for at least these states:
  - AR starting.
  - Character placed on shoulder.
  - Face temporarily lost / searching.
  - AR blocked or failed.
  - AR paused.
- `ArSessionStatus.Paused` maps to an interaction signal instead of being ignored.
- The top overlay still shows companion cue text first and technical AR status second.
- Unit tests cover the new reducer state.

## Non Goals

Do not add:

- Microphone permission.
- ASR.
- TTS.
- LLM/API calls.
- Networking.
- Persistence.
- New runtime dependencies.
- New character asset work.
- Placement tuning.
- ARCore or SceneView lifecycle changes.

Do not change the validated shoulder placement values in `CharacterPlacementProfiles`.

## Likely Files

- `app/src/main/java/com/angelmirror/interaction/CompanionInteraction.kt`
- `app/src/main/java/com/angelmirror/ui/AngelMirrorApp.kt`
- `app/src/test/java/com/angelmirror/BootstrapStatusTest.kt`
- `docs/backlog.md`
- `docs/architecture.md` only if the interaction boundary materially changes

## Suggested Implementation

1. Add a new `CompanionMood.Paused` or reuse a clearly named existing mood if it fits.
2. Add `CompanionSignal.ArSessionPaused`.
3. Add a `CompanionCues.Paused` cue with short user-facing copy.
4. Update `CompanionInteractionReducer.reduce` so paused state disables voice and LLM flags, same as every current state.
5. Update `ArSessionStatus.toCompanionSignal()` in `AngelMirrorApp.kt`:
   - `ArSessionStatus.Paused -> CompanionSignal.ArSessionPaused`
6. Add a unit test proving paused status maps to the paused cue.
7. Move B019 from Ready to Done in `docs/backlog.md`.

Keep copy short. Do not write explanatory UI text like "This screen shows...". Use product-facing text only.

## Validation

Run, if the local Android toolchain is available:

```sh
./scripts/preflight.sh
```

At minimum, run:

```sh
./scripts/docs-presence-check.sh
./scripts/format-check.sh
./scripts/dependency-validation-check.sh
./gradlew test
```

If Gradle cannot run locally, state the exact reason in the final handoff.

## Done Criteria

- The app compiles.
- Unit tests cover the new paused interaction state.
- Existing interaction tests still pass.
- No new dependencies are added.
- No AR placement values are changed.
- `docs/backlog.md` reflects B019 as done.
- Final handoff lists files changed and validation results.

## Handoff Notes

If the task cannot be completed, leave a short note with:

- What was changed.
- What remains.
- The exact command that failed.
- The first actionable error message.
