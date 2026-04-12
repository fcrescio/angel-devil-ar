# Coding Constraints

- Keep business logic out of composables.
- Do not introduce global singletons without a documented reason.
- Do not add dependencies without a backlog task and architectural note.
- Keep package ownership clear.
- Prefer interfaces only where they isolate AR, tracking, rendering, or future interaction seams.
- Avoid broad refactors during feature tasks.
- Use comments only for constraints, workarounds, or non-obvious decisions.
- Do not implement LLM, ASR, TTS, backend, auth, analytics, or complex persistence in bootstrap work.
