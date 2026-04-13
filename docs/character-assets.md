# Character Assets

## Current Asset

`app/src/main/assets/models/grotesque_imp.glb` is the current placeholder character asset.

It is a small procedural low-poly grotesque imp/devil with uneven horns, ragged wings, broken teeth, a kinked tail, and a cracked halo fragment. The asset was generated specifically for this repository from simple mesh primitives, so it is app-owned project art and has no external model license dependency.

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

- File exists at expected path.
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
