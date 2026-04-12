# Angel Mirror AR

Android app sperimentale che usa la camera frontale e ARCore Augmented Faces per mostrare un personaggio 3D flottante vicino alla spalla dell'utente.

## Obiettivo attuale

Preparare l'infrastruttura di sviluppo e una base tecnica solida per iterare in modo agentico. Questa fase e' bootstrap: documenti, CI, scheletro Android compilabile e confini architetturali.

## Stack

- Kotlin
- Jetpack Compose
- ARCore Augmented Faces
- SceneView
- Filament/gltfio tramite SceneView
- Asset 3D GLB/glTF

## Stato

Bootstrap repository. Nessuna feature prodotto completa e' implementata.

## Come lavorare

1. Leggi `AGENTS.md`.
2. Controlla `docs/project-brief.md` e `docs/architecture.md`.
3. Scegli un task atomico da `docs/backlog.md`.
4. Esegui `./scripts/preflight.sh` prima di consegnare.

## Comandi principali

```sh
./gradlew assembleDebug
./gradlew test
./gradlew lint
./scripts/preflight.sh
```

## Documenti principali

- `docs/project-brief.md`
- `docs/architecture.md`
- `docs/roadmap.md`
- `docs/backlog.md`
- `docs/decisions/`
- `docs/agentic/`
- `AGENTS.md`

## Non incluso in questa fase

- LLM
- ASR/TTS
- Backend
- Auth
- Analytics
- Persistence complessa
- Polishing artistico
- Ottimizzazione avanzata device-specific
