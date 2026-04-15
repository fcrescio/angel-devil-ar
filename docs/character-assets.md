# Character Assets

## Current Asset

`app/src/main/assets/models/trellis_winged_devil.glb` is the current runtime character asset for the devil profile.

It is a Trellis-generated winged devil model provided as `mesh.glb` and imported into the app as `trellis_winged_devil.glb`. It contains a textured skinned mesh with joints and weights, but no authored animation clips yet. The joint names are still generic (`joint_0`, `joint_1`, ...), so runtime bone animation needs a later mapping pass before it can target head, wings, or tail reliably.

The previous app-owned procedural placeholder remains in the repository for fallback/reference:

`app/src/main/assets/models/grotesque_imp.glb`

It is a small procedural low-poly grotesque imp/devil with uneven horns, ragged wings, broken teeth, a kinked tail, and a cracked halo fragment. The asset was generated specifically for this repository from simple mesh primitives, so it is app-owned project art and has no external model license dependency. The front of the model faces positive Z; wings and tail sit on negative Z so the shoulder character reads face-first in AR.

The previous technical placeholder remains in the repository for fallback/reference:

`app/src/main/assets/models/fox.glb`

## Previous Fox Placeholder

Source: Khronos glTF Sample Models, `2.0/Fox/glTF-Binary/Fox.glb`.

License notes from the upstream model README:

- Low poly fox by PixelMannen: CC0.
- Rigging and animation by @tomkranis on Sketchfab: CC-BY 4.0.
- glTF conversion by @AsoboStudio and @scurest.

The fox is a technical placeholder only. It is not final art direction for Angel Mirror AR.

## Policy

## Requirements

- Use GLB or glTF.
- Keep the first asset small enough for fast CI and debug install cycles.
- Store app-owned runtime assets under `app/src/main/assets/`.
- Record source, license, and any modifications here.
- Do not add generated binary variants without a task.

## Validation

Assets are validated by `scripts/validate-assets.sh`:

- Files exist at expected runtime/fallback paths.
- Size under 5MB.
- Valid GLB header with magic `glTF`.
- GLB version 2.
- Declared length matches actual file size.
- Contains JSON chunk (glTF data).
- Contains BIN chunk (binary data).

Run validation with:

```sh
./scripts/validate-assets.sh
```

## Generation And Review

Regenerate the app-owned placeholder with:

```sh
./scripts/generate-grotesque-imp.py
```

Render a quick multi-angle contact sheet with:

```sh
./scripts/render-character-asset.py app/src/main/assets/models/grotesque_imp.glb --output /tmp/angel-asset-renders/contact.png
```
