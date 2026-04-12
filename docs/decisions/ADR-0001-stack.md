# ADR-0001: Android AR Stack

## Status

Accepted.

## Decision

Use Kotlin, Jetpack Compose, ARCore Augmented Faces, SceneView, Filament/gltfio through SceneView, and GLB/glTF character assets.

## Context

The product is a selfie AR mirror. The important technical risk is front-camera face tracking and stable character rendering, not general-purpose game engine capability.

## Consequences

- The app remains native Android.
- Unity is out of scope.
- ARCore Augmented Faces is the first tracking target.
- SceneView owns most scene/rendering boilerplate.
- Character assets should enter the project as GLB/glTF.
