# Project Brief

## Product Vision

Angel Mirror AR is an Android selfie-AR mirror. The user opens the app, sees the front camera feed, and sees a small 3D character floating near their shoulder. Later milestones will add voice conversation, but the first technical goal is a stable AR mirror foundation.

## Target Experience

- Open the app on an ARCore-supported Android device.
- Grant camera permission.
- Start a front-camera AR session.
- Track the user's face/head with ARCore Augmented Faces.
- Render a GLB character through SceneView.
- Place the character relative to the face/head pose with simple offset and smoothing.

## Current Phase

M0 bootstrap. The repository must be easy for humans and agents to extend without hidden architectural decisions.

## Technical V0/V1 Scope

- Front camera.
- ARCore Augmented Faces.
- SceneView rendering.
- GLB/glTF character asset pipeline.
- Shoulder-relative placement driven by face/head pose.
- Clean boundaries for future voice and LLM work.

## Non Goals

- LLM integration.
- ASR/TTS.
- Backend.
- Auth.
- Analytics.
- Complex persistence.
- Advanced device-specific optimization.
- Polished art direction.
- Custom tracking engine.

## Glossary

- Augmented Faces: ARCore face tracking feature used by the front camera flow.
- SceneView: Android 3D/AR rendering library used to avoid Unity and direct Filament boilerplate.
- GLB: Binary glTF asset format for 3D characters.
- Shoulder-relative placement: App-managed offset from the tracked face/head pose, not world anchoring.
