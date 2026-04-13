# Roadmap

## M0 - Bootstrap

Repository structure, Android skeleton, CI, docs, ADRs, templates, and agentic workflow.

## M1 - Camera And AR Readiness

Launchable app, camera permission flow, ARCore availability checks, and clear unsupported-device states.

## M2 - Face Session

Front camera AR session using ARCore Augmented Faces. No character placement yet.

## M3 - Static Character

Load a GLB character and render it through SceneView with a static test placement.

## M4 - Shoulder-Relative Placement

Use face/head pose, offset, and smoothing to place the character near the shoulder.

## M5 - Interaction Prep

Add interfaces and state boundaries that can later support voice and LLM integration without adding those systems yet.

Initial work is in place: the app now has a pure Kotlin companion interaction reducer and cue model, while voice input and LLM responses remain disabled by design.
