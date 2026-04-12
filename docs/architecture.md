# Architecture

## Overview

The bootstrap uses a single Android app module. Internal package boundaries are explicit so future tasks can stay small without introducing a premature multi-module setup.

## Logical Packages

- `app`: app-level state, entry wiring, and navigation decisions.
- `ui`: Compose UI and lightweight UI state.
- `ar`: ARCore session lifecycle, SceneView bridge, and renderer ownership.
- `tracking`: face/head pose data, smoothing, and pose transforms.
- `character`: GLB asset loading, character node ownership, animation hooks, and placement rules.
- `permissions`: camera permission and ARCore availability checks.
- `util`: small shared helpers only.

## Initial Data Flow

1. UI requests camera/AR readiness through `permissions`.
2. `ar` creates and owns the AR session lifecycle.
3. ARCore Augmented Faces produces face/head pose data.
4. `tracking` converts raw pose data into stable app-level pose objects.
5. `character` applies shoulder-relative placement and exposes renderable character state.
6. `ui` displays state and hosts the AR surface.

## Placement Constraint

The character is not initially world anchored in the classic AR sense. The v1 placement is relative to the user's face/head pose with an app-defined offset and smoothing. Do not introduce world tracking, body tracking, or a custom tracking engine for M1-M4 work.

## Dependency Rule

New runtime dependencies require a backlog task and, when they affect architecture, an ADR.

## Open Points

- Initial GLB placeholder asset source.
- Smoothing algorithm and offset defaults.
- Device support policy for non-ARCore devices.

## Current AR Host

The first AR host uses SceneView `ARSceneView` with `Session.Feature.FRONT_CAMERA` and ARCore Augmented Faces set to `MESH3D`. Plane finding and light estimation are disabled for this first selfie flow. The host reports session lifecycle and tracking status to Compose, but it does not yet attach a character model or solve final shoulder placement in the render tree.
