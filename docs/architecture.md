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

## Placement Baseline

The current validated placement profile is `pixel7-ear-shoulder`:

- horizontal offset: `0.15m`
- vertical offset: `-0.01m`
- depth offset: `-0.08m`
- placeholder scale: `0.18`

The AR screen includes a lightweight debug overlay toggle that reports the active profile, offset, and latest smoothed model placement.

## Interaction Boundary

The first interaction layer is intentionally local and deterministic. `CompanionInteractionReducer` accepts simple AR/character signals and returns a `CompanionInteractionState` containing the current cue, mood, and disabled voice/LLM flags. This gives future ASR/TTS/LLM work a stable app boundary without adding those systems to the AR rendering path.
