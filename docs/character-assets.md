# Character Assets

## Current Asset

`app/src/main/assets/models/fox.glb` is the first placeholder character asset.

Source: Khronos glTF Sample Models, `2.0/Fox/glTF-Binary/Fox.glb`.

License notes from the upstream model README:

- Low poly fox by PixelMannen: CC0.
- Rigging and animation by @tomkranis on Sketchfab: CC-BY 4.0.
- glTF conversion by @AsoboStudio and @scurest.

This is a technical placeholder only. It is not final art direction for Angel Mirror AR.

## Policy

## Requirements

- Use GLB or glTF.
- Keep the first asset small enough for fast CI and debug install cycles.
- Store app-owned runtime assets under `app/src/main/assets/`.
- Record source, license, and any modifications here.
- Do not add generated binary variants without a task.
