# AGENTS.md

## Project Mission

Build Angel Mirror AR, an Android selfie-AR mirror app with a floating 3D character near the user's shoulder. The app will eventually support voice interaction, but the current phase is repository bootstrap and technical skeleton only.

## Read This First

1. `docs/project-brief.md`
2. `docs/architecture.md`
3. `docs/decisions/*.md`
4. `docs/backlog.md`
5. `docs/agentic/coding-constraints.md`

## Source Of Truth

Architecture and product scope come from:

1. `docs/project-brief.md`
2. `docs/architecture.md`
3. ADRs in `docs/decisions/`
4. `docs/backlog.md`

If these disagree, update the lower-authority document or ask for clarification before expanding scope.

## Hard Constraints

- Use Kotlin, Jetpack Compose, ARCore Augmented Faces, SceneView, and GLB/glTF assets.
- Do not introduce Unity.
- Do not add LLM, ASR, TTS, backend, auth, analytics, or complex persistence unless explicitly requested.
- Do not change the stack without an ADR and explicit task.
- Keep changes small, localized, and verifiable.
- Update docs when behavior, package boundaries, or architectural decisions change.

## Package Boundaries

- `app`: app-level state and wiring.
- `ui`: Compose screens and lightweight state holders.
- `ar`: SceneView and ARCore session integration.
- `tracking`: face/head pose abstractions and smoothing contracts.
- `character`: GLB character assets, nodes, animation, and placement.
- `permissions`: camera and AR availability permission flows.
- `util`: small shared helpers with no domain ownership.

## Before Coding

- Check whether the task exists in `docs/backlog.md`.
- Confirm which package owns the change.
- Prefer existing patterns over new abstractions.
- Avoid wide refactors unless the task explicitly asks for them.

## After Coding

- Run `./scripts/preflight.sh` when the local Android toolchain is available.
- Declare changed files and why they changed.
- Update `docs/backlog.md` after meaningful task progress.
- Leave handoff notes when work is incomplete.

## Done Criteria

- Debug build passes.
- Unit tests pass.
- Android lint passes when applicable.
- Docs are in sync.
- No obvious dead code or unowned behavior.
