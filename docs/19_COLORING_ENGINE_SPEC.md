# 19 — Coloring Engine V1 Contract

**Status:** Phase 0.5 implementation contract  
**Scope:** Public V1 guided/self coloring after line-art drawing plus future reuse in coloring-only content.

## 1. Objective

Coloring Engine owns editable color operations, supported fill-region semantics, guided-coloring progression, and safe persistence for the coloring phase.

It must let a child move from completed line art into coloring without flattening or losing the editable drawing document.

Coloring is not implemented as a screenshot/bitmap paint layer owned by UI.

## 2. Responsibility boundary

Coloring Engine owns:
- coloring session state;
- active color/tool selection semantics;
- freehand color strokes;
- authored fill-region operations;
- undo/redo integration for coloring operations;
- guided vs self-color mode;
- coloring-step progress where a lesson supplies authored coloring steps;
- save/resume of the coloring phase;
- completion handoff back to Lesson Engine / artwork completion flow.

Coloring Engine does **not** own:
- screen navigation;
- lesson drawing-step sequencing;
- companion animation;
- raw Android pointer dispatch;
- line-art stroke storage format;
- public sharing/export.

## 3. Composition model

A final editable artwork is composed from stable layers/roles rather than one destructive bitmap:

```text
Teacher/guide overlays        transient, never saved as artwork
Final line art                Drawing Engine operations
Color strokes/fills           Coloring Engine operations
Background/paper              document/background role
```

Rendering order for most V1 lessons:

```text
background
  ↓
color fills / color strokes
  ↓
line art
  ↓
transient guides
```

A lesson may declare a different authored composition policy only through validated content metadata; UI must not reorder layers ad hoc.

## 4. Shared document model

Coloring writes into the same authoritative `DrawingDocument` operation stream used by Drawing Engine.

Color operation types:
- `AddColorStroke`
- `SetRegionFill`
- `AddColorEraseMask` or shared erase-mask operation with target role
- `ClearColoring` as an undoable history command

The document remains one editable artwork with operation roles rather than separate incompatible drawing/coloring files.

## 5. Color stroke model

Freehand coloring reuses the Drawing Engine input/render substrate behind product-level coloring tools.

`ColorStrokeRecord` conceptually contains:
- stable operation/stroke ID;
- brush preset ID;
- semantic/actual color value;
- opacity;
- base size;
- serialized input payload/reference;
- target role = coloring;
- optional bounds cache.

V1 visible coloring presets can map to the same low-level Ink brush infrastructure as drawing while remaining separate product presets.

Suggested V1 product presets:
- `color.crayon`
- `color.marker`
- `color.soft_brush` only if performance/UX proves strong
- `color.eraser`

Age policy decides which are visible.

## 6. Authored fill-region model

### Why regions are authored

V1 must not depend on unreliable image flood-fill over anti-aliased freehand line art. Lessons that support one-tap fill therefore ship explicit authored regions.

A lesson/package may include `ColorRegion` definitions:
- `regionId` stable within lesson revision;
- vector boundary/mask asset reference;
- optional semantic role such as `body`, `shirt`, `sky`;
- optional suggested palette roles;
- optional guided-coloring step membership.

Compiled content can convert source vector regions into a runtime-efficient representation such as vector paths or masks while preserving stable region IDs.

### Fill operation

`SetRegionFill(regionId, color)` stores semantic operation data, not a permanently baked bitmap.

Rules:
- filling the same region again replaces/updates the effective region color through history semantics;
- undo restores the previous effective fill;
- region fill must remain deterministic across save/load;
- a missing/corrupt optional region must not destroy line art or freehand color strokes.

## 7. Guided coloring mode

Entered through `Color With Me`.

Lesson content may supply `coloringSteps`.

A guided coloring step can define:
- target region IDs or optional suggested area;
- narration key;
- suggested palette roles/colors;
- technique hint;
- whether freehand coloring is allowed in that step;
- completion policy (`manual_done`, `region_filled`, or other deterministic supported rule).

The engine exposes current guided step and completion eligibility.

### Product principle

Suggested colors are teaching prompts, not arbitrary correctness tests.

Unless the lesson objective is explicitly color recognition/theory:
- the child may choose another color;
- the app never marks the artwork wrong for creative color choice;
- companion language remains suggestive rather than punitive.

## 8. Self-color mode

Entered through `Color Myself` or by switching from guided mode where allowed.

Self-color mode:
- removes step progression requirements;
- keeps all authored fill regions available if the lesson supports them;
- allows supported freehand coloring tools;
- retains undo/redo and save/resume;
- does not remove or flatten completed guided color work.

Switching from guided → self is one-way for V1 unless the session model can safely preserve guided-step state. The public UI should not promise a reversible switch until tested.

## 9. Palette model

The engine stores actual color values; UI/policy decides palette presentation.

A lesson can provide:
- recommended semantic palette roles;
- authored swatches;
- optional constrained palette for a teaching objective.

Global/age palettes can provide defaults.

Rules:
- critical state is never conveyed by color alone;
- palette choice must work offline;
- lesson palette data does not require remote resolution;
- parent/child profile may later store favorite colors without altering content schema semantics.

## 10. Erasing in coloring

Color erasing must not erase final line art by default.

The eraser target is role-aware:
- coloring eraser affects coloring strokes/masks only;
- drawing eraser in the drawing phase affects child line-art operations;
- teacher/guide overlays are immutable/transient.

If a future advanced tool intentionally edits line art during coloring, that is a separate explicit capability, not V1 default behavior.

## 11. Undo/redo

Coloring shares the document/history infrastructure with Drawing Engine.

Undoable operations include:
- add color stroke;
- erase color;
- set/replace region fill;
- clear coloring;
- optional guided step completion marker if required for session behavior.

History rules:
- undo never removes immutable lesson/teacher content;
- redo branch clears after a new edit following undo;
- history remains operation-based, not full-bitmap snapshots;
- crossing the drawing→coloring phase boundary must not make earlier child drawing operations mysteriously disappear from history.

Public V1 may scope undo UI to the current coloring session if a full cross-phase history is confusing, but the persisted artwork must remain structurally valid and editable.

## 12. Drawing → coloring handoff

Atomic ordering:

1. Lesson Engine enters `DrawingComplete` only after Drawing Engine confirms the final required child document commit.
2. Lesson Engine persists a session snapshot marking drawing phase complete.
3. Child chooses guided coloring / self coloring / finish.
4. For coloring, Coloring Engine loads the existing child document ID and lesson coloring metadata.
5. Coloring Engine validates region assets/step metadata.
6. Session snapshot updates to coloring phase.
7. Coloring workspace opens only after the engine reports `Ready`.

If Coloring Engine initialization fails:
- child line art remains saved;
- child can Retry, Finish for Now, or return safely;
- failure cannot invalidate the completed drawing.

## 13. Coloring session state

Conceptual states:

```text
Uninitialized
Loading
Ready
Guided
  ├── PreparingColorStep
  ├── AwaitingChildColor
  └── CompletingColorStep
SelfColoring
Paused(previousStableState)
Completed
RecoverableError
FatalContentError
```

State also carries:
- document ID;
- lesson/revision when relevant;
- coloring mode;
- current guided coloring step ID/index;
- selected color;
- selected coloring tool;
- completion/progress information.

UI observes engine state; UI does not invent a parallel coloring state machine.

## 14. Commands

Conceptual commands:
- `LoadColoringSession(documentId, lessonId?, mode)`
- `SetColor(color)`
- `SetColorTool(tool)`
- `FillRegion(regionId)`
- `Undo`
- `Redo`
- `ClearColoring`
- `MarkColorStepDone`
- `SwitchToSelfColoring`
- `Pause`
- `Resume`
- `SaveAndExit`
- `CompleteColoring`

Pointer-driven freehand strokes are committed through the shared DrawingSurface/engine adapter rather than routing every pointer event through feature UI/ViewModel.

## 15. Events/effects

Examples:
- `ColorStepChanged`
- `ColoringAutosaveRequested`
- `NarrationRequested`
- `CompanionSignal`
- `RegionFillRejected(reason)`
- `ColoringCompleted`
- `ColoringRecoveryRequired`

Effects do not become state-machine truth.

## 16. Persistence/resume

Session snapshot stores:
- document ID;
- lesson ID/revision where relevant;
- phase = coloring;
- mode guided/self;
- guided color step when relevant;
- selected tool/color if useful;
- completion state.

Artwork operations remain in the authoritative document store.

On resume:
1. load document;
2. validate coloring metadata for the recorded lesson revision;
3. restore a safe stable coloring state;
4. clear transient guides;
5. restore guided-step prompt if applicable;
6. preserve every committed color operation.

## 17. Error behavior

### Recoverable
- missing narration;
- optional suggested-palette asset failure;
- temporary renderer/preview issue;
- one invalid optional region when self-coloring can continue safely.

### Fatal for guided coloring, not artwork
- required guided region asset missing/corrupt;
- incompatible required coloring-step metadata.

Fatal coloring-content failure never means fatal artwork loss. Preserve the drawing/color work and offer safe finish/exit.

## 18. Engine invariants

Always true:
- line art is not flattened merely to enter coloring;
- teacher/guide overlays never become artwork;
- color eraser cannot silently erase line art;
- region IDs are stable for one lesson revision;
- fill results survive save/load deterministically;
- UI does not own guided coloring progression;
- network is not required for bundled coloring;
- failure in coloring cannot corrupt a successfully completed drawing.

## 19. Test matrix

Automated tests must cover:
- guided and self modes;
- region fill + replace + undo + redo;
- freehand color stroke + erase + undo;
- clear coloring undo;
- switch guided → self;
- save/restore in every stable state;
- missing optional region fallback;
- missing required guided region recovery;
- drawing→coloring handoff failure/retry;
- completion with only fills;
- completion with only freehand color;
- mixed fills and freehand color;
- process recreation;
- line-art protection from color eraser.

## 20. V1 acceptance gate

Coloring Engine V1 contract is satisfied when:
- completed child line art enters coloring without flattening/data loss;
- both guided and self-color flows work from the same artwork document;
- authored regions fill deterministically and remain editable;
- freehand coloring uses the shared low-latency drawing substrate;
- coloring eraser respects role boundaries;
- undo/redo/save/resume are stable;
- guided progression is engine-owned;
- coloring failure cannot corrupt or strand the drawing;
- automated state/history tests are green.

## 21. Explicitly later

- advanced blending modes;
- unrestricted layer UI;
- gradient fills;
- texture/pattern fill library;
- AI auto-color suggestions;
- semantic image segmentation of arbitrary freehand drawings;
- real-time color harmony scoring;
- collaborative coloring.