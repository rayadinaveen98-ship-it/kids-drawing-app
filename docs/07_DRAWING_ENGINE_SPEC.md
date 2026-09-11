# 07 — Drawing Engine 0.1 Implementation Contract

**Status:** Phase 0.5 contract  
**Substrate:** AndroidX Ink 1.0.0 stable behind owned domain interfaces  
**See:** ADR-003 and ADR-007

## 1. Objective

Drawing Engine 0.1 must prove that the product can capture, render, persist, undo/redo and deterministically replay child/teacher strokes with sufficiently low latency for a drawing tutor.

The engine is a product/domain module. UI does not own stroke history, playback timing or document persistence.

## 2. Architecture boundary

```text
Compose application UI
        │
        ▼
DrawingSurfaceController / DrawingEngine API
        │
        ├── Document Store
        ├── History Engine
        ├── Playback Engine
        ├── Tool/Brush Registry
        ├── Coordinate Mapper
        └── Ink Adapter
                 │
                 ▼
        AndroidX Ink 1.0.0 stable
        + Android MotionEvent / graphics primitives
```

AndroidX Ink types should remain inside `core:drawing` infrastructure as much as practical. Lesson Engine, Gallery and feature UI consume our own domain models/IDs.

## 3. Logical coordinate system

Every drawing document has a logical width and height independent of physical device pixels.

Example lesson document: `1000 x 1000` logical units.

Runtime maintains transforms:
- document → viewport;
- viewport → document;
- optional viewport zoom/pan transform;
- lesson/content → document where lesson canvas matches or is mapped explicitly.

All persisted child/teacher semantic coordinates live in document space. Device density, orientation or viewport size must never be baked into stored stroke geometry.

## 4. Core domain model

### `DrawingDocument`
Required conceptual fields:
- `documentSchemaVersion`
- `documentId`
- `logicalWidth`
- `logicalHeight`
- `createdAt`
- `modifiedAt`
- `backgroundRole`
- ordered `operations`
- optional lesson provenance (`lessonId`, lesson revision)
- optional artwork/session metadata reference

### `DocumentOperation`
V1 engine recognizes at least:
- `AddInkStroke`
- `AddEraseMask`
- `RemoveOperation` / history representation as implementation detail
- `SetRegionFill` for coloring integration later
- `ClearDocument` represented through history rather than destructive data loss

The persisted document is the authoritative editable artwork representation. Flattened PNG/WebP previews are derivatives.

### `InkStrokeRecord`
Conceptual fields:
- stable `strokeId`
- author role (`child`, `teacher_generated` only where a document intentionally stores it)
- `brushPresetId`
- color/semantic color value
- opacity
- base size
- serialized input batch payload/reference
- z/order index implied by operation order
- optional bounds cache
- optional tool source metadata (finger/stylus/eraser)

Stroke input points preserve timing and optional pressure/tilt/orientation when available. AndroidX Ink `StrokeInputBatch` serialization is the preferred low-level payload encoding for stable Ink-backed strokes.

### `EraseMaskRecord`
Public V1 needs visual partial erasing without depending on alpha-only Ink geometry mutation.

An erase operation therefore stores an authored eraser path/mask in document coordinates. Rendering composites that mask against eligible user-color/ink content. Undo removes the erase operation rather than attempting destructive reconstruction.

Art Lab 0.1 may begin with whole-stroke erase for infrastructure validation, but the public V1 contract remains partial visual erasing through owned mask semantics.

## 5. Brush/tool abstraction

UI references product-level tool presets, never raw renderer configuration.

Initial presets:
- `pencil.standard`
- `crayon.standard`
- `marker.standard`
- `eraser.standard`

A `BrushPreset` resolves:
- Ink brush family/configuration;
- default size range;
- pressure behavior;
- opacity behavior;
- stylus tilt behavior when supported;
- child-facing allowed controls.

Age policy controls which presets/settings are exposed; Drawing Engine only enforces technical bounds.

## 6. Input pipeline

### Finger
- single primary drawing pointer creates ink;
- secondary pointers are reserved for future pan/zoom gestures and must not accidentally create marks;
- pointer cancellation must terminate/cancel the in-progress stroke safely.

### Stylus
When supported, preserve/use:
- tool type;
- pressure;
- tilt;
- orientation where useful;
- stylus eraser tool type when provided.

During an active stylus stroke, finger/palm contacts must not create accidental marks. Exact palm-rejection policy is implemented/tested using the stable Ink/input path and platform tool types.

### Event handling
The low-level adapter consumes `MotionEvent` input through the stable Ink authoring path. Completed strokes are converted into our `InkStrokeRecord` and committed atomically to the document/history layer.

No per-point database writes occur on the input hot path.

## 7. Rendering architecture

Phase 1 should start with a **View-backed low-latency inking surface hosted inside Compose** behind a `DrawingSurface` adapter rather than forcing all drawing onto normal Compose recomposition.

Conceptual layers:

```text
Guide / lesson overlay         (not artwork)
In-progress low-latency ink    (transient)
Committed document ink         (persistent render)
Color/fill layer               (later Coloring Engine)
Background/paper
```

Actual ordering may place coloring below final line art; the renderer owns composition, not feature screens.

Completed Ink strokes use stable Ink rendering APIs. In-progress strokes use stable Ink authoring/low-latency rendering. We may use `androidx.graphics` low-latency primitives behind the adapter if profiling justifies it, without leaking them into product APIs.

## 8. Stroke commit lifecycle

1. Pointer down starts an in-progress stroke with resolved tool preset.
2. Move events update the transient low-latency stroke.
3. Pointer up finishes input.
4. Stable Ink output/input batch is converted to an owned stroke record.
5. `AddInkStroke` operation is appended atomically.
6. History receives one undoable command for the completed stroke.
7. Persist/autosave is scheduled off the input/render hot path.
8. Transient renderer relinquishes the stroke once committed rendering is confirmed.

An interrupted/cancelled stroke is either explicitly cancelled or committed only if it is valid according to engine rules; it must never leave a half-referenced document entry.

## 9. Undo/redo model

History is command/operation based, not bitmap snapshot based.

V1 undoable actions include:
- add stroke;
- erase mask;
- clear document;
- coloring operation when integrated.

Rules:
- Undo/redo never changes immutable source lesson content.
- New edits after undo clear the redo branch.
- Clear is undoable.
- History has a configurable in-memory depth but document persistence remains complete.
- Large history must not duplicate full bitmap snapshots per action.

## 10. Persistence

### Authoritative format
A versioned document envelope stores:
- document metadata;
- ordered operation metadata;
- brush/tool identifiers;
- Ink stroke input payloads;
- eraser paths/masks;
- future coloring operations.

The envelope may use protobuf/CBOR/JSON + binary stroke payloads; exact binary container is selected during Art Lab after size/load benchmarking. The domain schema and migration strategy are mandatory regardless of container.

### Save strategy
- atomic write to a temporary file then replace/rename;
- never overwrite the only known-good document with a partially written file;
- save on stable editing boundaries and lifecycle events;
- autosave is throttled/debounced but no long session relies only on Activity destruction callbacks;
- preview image generation is asynchronous and non-authoritative.

### Recovery
If the newest autosave is corrupt, attempt the previous known-good snapshot/journal where implemented. Corrupt one-artwork data must not prevent the application/gallery from opening.

## 11. Deterministic teacher stroke playback

Teacher playback uses canonical lesson stroke input/timing, not a pre-recorded video.

### Source
A teacher stroke contains ordered input samples with source-relative timestamps in lesson/document coordinates.

### Playback clock
Playback Engine owns a monotonic virtual clock:

`sourceTime = accumulatedPlaybackTime × paceMultiplier`

Pause freezes virtual time. Resume continues from the exact virtual position. Changing pace changes future clock progression without resetting geometry.

### Pace multipliers — initial contract
- Extra Slow: `0.40x`
- Slow: `0.70x`
- Normal: `1.00x`
- Fast: `1.50x`
- Very Fast: `2.00x`

These multipliers control stroke reveal only. Lesson Engine separately controls narration and between-step teaching cadence.

### Determinism
- no predicted inputs are persisted into teacher source data;
- replay always derives from the same canonical source samples;
- final completed geometry/appearance must be equivalent regardless of playback pace within renderer tolerance;
- pause/resume cannot duplicate or skip source samples;
- repeated replay cannot mutate source lesson data.

## 12. Teacher vs child drawing separation

Teacher demonstrations are overlays by default and do **not** become part of the child's saved artwork.

Trace guides, anchors and teacher preview strokes live in a non-destructive lesson overlay layer. Only explicitly authored product behavior may copy/generated content into a final document.

This prevents replay/help actions from contaminating the child's art or undo history.

## 13. Zoom/pan

Art Lab 0.1 does not require user-facing zoom/pan. The engine coordinate architecture must support transforms from day one so adding zoom/pan later does not invalidate stored documents.

No input point is stored in raw screen coordinates.

## 14. Threading and allocation rules

- input dispatch and in-progress rendering remain lightweight;
- disk/database work never runs on the input hot path;
- avoid allocating per-point objects where the Ink/input APIs provide reusable/batched structures;
- expensive preview generation and serialization happen on background dispatchers;
- document mutations are serialized through one engine-owned mutation context to prevent concurrent history corruption.

## 15. Public engine API direction

Conceptual API, exact Kotlin naming may evolve:

```text
DrawingEngine
  observeDocument(): StateFlow<DrawingDocumentState>
  setTool(ToolSelection)
  setColor(...)
  setSize(...)
  undo()
  redo()
  clear()
  save()
  load(documentId)
  startTeacherPlayback(strokeSequence, pace)
  pauseTeacherPlayback()
  resumeTeacherPlayback()
  replayTeacherPlayback()
  setPlaybackPace(pace)
```

Pointer input itself can remain owned by the DrawingSurface/Ink adapter rather than passing every MotionEvent through a ViewModel.

## 16. Art Lab 0.1 required controls

Internal-only engineering UI:
- blank document;
- Pencil;
- Eraser;
- color;
- width;
- Undo;
- Redo;
- Clear;
- Save;
- Reload;
- record/import a teacher test stroke;
- Extra Slow / Slow / Normal / Fast / Very Fast;
- Play;
- Pause;
- Resume;
- Replay;
- debug metrics overlay toggle.

No production visual polish is required for Art Lab.

## 17. Drawing Engine 0.1 acceptance gate

Functional:
- no lost committed strokes in normal interaction;
- undo/redo stable across repeated sequences;
- clear is undoable;
- save/load round-trip preserves operation order and visible artwork;
- teacher playback works at all five paces;
- pause/resume and mid-playback pace changes are stable;
- app background/foreground does not corrupt the document;
- finger and supported stylus paths both work.

Architecture:
- feature UI contains no authoritative stroke history;
- Lesson Engine can invoke teacher playback without importing Ink types;
- saved artwork remains editable after restart;
- teacher overlays never enter child artwork accidentally.

Performance/reliability:
- must pass `docs/18_DRAWING_PERFORMANCE_GATES.md` before Drawing Engine 0.1 is locked.

## 18. Explicitly later than Drawing Engine 0.1

- lasso/selection;
- transforms of selected strokes;
- exposed layers;
- advanced brush designer;
- perspective/symmetry guides;
- multi-document tabs;
- collaborative drawing;
- strict stroke-shape grading;
- alpha-only partial-stroke Ink APIs.

The architecture may prepare for these without implementing them.