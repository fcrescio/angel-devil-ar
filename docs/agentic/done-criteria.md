# Done Criteria

A task is done when:

- The change is scoped to the requested behavior.
- `./gradlew assembleDebug` passes when the Android toolchain is available.
- `./gradlew test` passes when the Android toolchain is available.
- `./gradlew lint` passes when the Android toolchain is available.
- `./scripts/docs-presence-check.sh` passes.
- Documentation and backlog status are updated when needed.
- The final handoff states what was changed and what was validated.
