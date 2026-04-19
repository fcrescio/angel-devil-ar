# Architecture

## Overview

The bootstrap uses a single Android app module. Internal package boundaries are explicit so future tasks can stay small without introducing a premature multi-module setup.

## Logical Packages

- `app`: app-level state, entry wiring, and navigation decisions.
- `ui`: Compose UI and lightweight UI state.
- `ar`: ARCore session lifecycle, SceneView bridge, and renderer ownership.
- `tracking`: face/head pose data, smoothing, and pose transforms.
- `character`: GLB asset loading, character node ownership, animation hooks, and placement rules.
- `interaction`: companion mood, cues, and reducer boundaries for future voice/LLM behavior.
- `permissions`: camera permission and ARCore availability checks.
- `util`: small shared helpers only.

## Initial Data Flow

1. UI requests camera/AR readiness through `permissions`.
2. `ar` creates and owns the AR session lifecycle.
3. ARCore Augmented Faces produces face/head pose data.
4. `tracking` converts raw pose data into stable app-level pose objects.
5. `character` applies shoulder-relative placement and exposes renderable character state.
6. `interaction` maps AR/character signals into companion-facing cues without invoking voice or LLM systems.
7. `ui` displays state and hosts the AR surface.

## Placement Constraint

The character is not initially world anchored in the classic AR sense. The v1 placement is relative to the user's face/head pose with an app-defined offset and smoothing. Do not introduce world tracking, body tracking, or a custom tracking engine for M1-M4 work.

## Dependency Rule

New runtime dependencies require a backlog task and, when they affect architecture, an ADR.

## Open Points

- Initial GLB placeholder asset source.
- Smoothing algorithm and offset defaults.
- Device support policy for non-ARCore devices.

## Current AR Host

The first AR host uses SceneView `ARSceneView` with `Session.Feature.FRONT_CAMERA` and ARCore Augmented Faces set to `MESH3D`. Plane finding and light estimation are disabled for this first selfie flow. The host reports session lifecycle and tracking status to Compose and attaches a small GLB placeholder model. On each AR frame, the character controller reads the tracked `AugmentedFace.centerPose`, applies a side-and-up offset intended to sit near shoulder/ear height, smooths movement, and updates the model node position.

The devil presentation uses a warm front-low key light plus elevated indirect
light. The Trellis asset has a naturally dark red/black texture, so the AR host
keeps the light stronger than a neutral model would need while preserving the
front-low horror read. The same presentation profile also carries the asset
orientation correction: the Trellis mesh is yawed `180` degrees at runtime so
its face, not its tail and wings, points toward the selfie camera.

The Trellis devil uses a runtime procedural rig animator for the current motion
vocabulary. `DevilProceduralMotion` owns the deterministic pose curves for
`Appearing`, `Idle`, `Searching`, `Blocked`, and `Paused`; `DevilRigAnimator`
applies those poses to the validated wing and tail joint subset. This remains a
temporary asset-specific layer until the project has a curated rig with authored
GLB animation clips.

## Placement Baseline

The current validated placement profile is `pixel7-ear-shoulder`:

- horizontal offset: `0.15m`
- vertical offset: `-0.01m`
- depth offset: `-0.08m`
- placeholder scale: `0.18`

The AR screen includes a lightweight debug overlay toggle that reports the active profile, offset, and latest smoothed model placement.

## Interaction Boundary

The first interaction layer is intentionally local and deterministic. `CompanionInteractionReducer` accepts simple AR/character signals and returns a `CompanionInteractionState` containing the current cue, mood, and disabled voice/LLM flags. This gives future ASR/TTS/LLM work a stable app boundary without adding those systems to the AR rendering path.

The first manual interaction surface is a bottom quick-action bar in the AR
screen. It sends deterministic `CompanionSignal` values such as greeting,
provocation, and reassurance into the same reducer used by AR lifecycle events.
Those actions only change companion cue and mood; voice input and LLM responses
remain explicitly disabled.

Manual greeting and reassurance use distinct companion moods so the character
can react differently from the default idle loop. Greeting maps to a more active
wing/body acknowledgement, reassurance maps to a quieter settling pose, and
provocation keeps using the blocked/agitated reaction.

Manual reactions are transient. Each manual cue carries a local duration and
`CompanionReactionEngine` returns the expiry request that the Compose host should
schedule. The reducer only accepts expiry for the currently active cue, so stale
timers cannot override newer AR or user signals. Repeated taps on the same
action build a small capped streak that adjusts local copy and is surfaced in
the debug overlay together with the active interaction mood.

The character layer receives a `CharacterAnimationDirective` rather than only a
raw intent. The directive carries a clamped reaction intensity derived from the
manual-action streak, allowing `Boo` level 2/3 to increase body twitch and tail
motion while keeping the shoulder placement stable. Greeting and calming also
read the directive, but stay within smaller motion bounds.

Debug builds expose the git short hash through `BuildInfo`, display it in the
readiness and AR overlays, and set `versionName` to include the same hash. The
local install script builds a debug APK for the current commit and checks the
installed `versionName` when a device is available. GitHub Actions uploads the
debug APK artifact with the commit short hash in the artifact name.
