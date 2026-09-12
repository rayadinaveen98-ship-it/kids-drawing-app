# P3.4 — Vertical-slice Coloring + Safe Handoff Execution Contract

**Issue:** #46  
**Parent:** #42  
**Depends on:** P3.3 / #45  
**Status:** implementation contract

## Objective

Ship the first real drawing→coloring vertical slice without flattening or weakening the editable DrawingDocument contract. A completed Cute Cat drawing must enter a child-safe coloring workspace, preserve protected line art, persist color work, and recover safely after interruption.

## Product path

1. Child completes Cute Cat drawing.
2. Lesson Engine owns the post-drawing choice.
3. Product persists the child drawing before any coloring transition.
4. Child may choose Color With Me, Color Myself, or Finish for now.
5. A coloring choice dispatches the real Lesson Engine command.
6. Product receives the real `ColoringHandoffRequested` mode/document contract.
7. Coloring Engine validates the same editable child document and persists its own semantic session.
8. Only after successful Coloring Engine initialization does product acknowledge `ColoringHandoffCompleted` to Lesson Engine.
9. Coloring Engine then owns coloring-session progression while DrawingDocumentEngine remains artwork authority.
10. Failure before successful initialization sends `ColoringHandoffFailed`; Lesson Engine returns safely to post-drawing choice with the drawing intact.

## Artwork representation

The DrawingDocument remains the single authoritative editable artwork.

P3.4 extends the operation vocabulary with explicit coloring operations rather than overloading normal drawing operations:
- `AddInkStroke` — protected drawing/line-art stroke;
- `AddEraseMask` — drawing-stage erase mask;
- `AddColorStroke` — child coloring stroke;
- `AddColorEraseMask` — erases coloring content only;
- `ClearDocument` — existing global operation; never exposed in the coloring workspace.

This makes role protection structural and persisted rather than inferred from screen state or color values.

### Schema compatibility

- bump DrawingDocument schema from 1 → 2;
- existing schema-1 documents decode with all existing operations interpreted exactly as before;
- color operations are valid only in schema 2+;
- any new mutation migrates a loaded document to the current schema before save;
- keep the existing checksummed atomic envelope/store and stroke payload format.

## Rendering/compositing

Required visual composition:

```text
lesson/teacher/guide overlay      transient, never artwork
protected line-art projection     persisted drawing operations
color projection                  persisted coloring operations
paper/background
```

The renderer must composite color **below** protected line art.

A color erase mask operates only against the color projection. It must never clear line-art pixels.

The existing fast line-art projection path and Drawing Engine performance gates must remain intact. Layered rendering may use a separate color raster that is activated only when color operations exist.

## Coloring Engine boundary

UI does not own coloring progression truth.

A narrow `ColoringSessionEngine`/runtime owns:
- document identity;
- Color With Me vs Color Myself semantic mode;
- active/finished session phase;
- selected palette color and size defaults where persisted product semantics require them;
- safe coloring-only commit/erase/undo/redo permissions;
- save/background/recovery state.

DrawingToolEngine may still own low-level selected pencil/eraser configuration; it does not own the coloring session lifecycle.

## Safe history rules

Coloring Undo/Redo must never cross into protected drawing operations.

- `undoColoring()` succeeds only when the current last operation is a coloring operation;
- it stops at the protected drawing boundary;
- `redoColoring()` can restore only coloring operations removed through the coloring path;
- a new coloring edit clears the redo branch through the existing DrawingDocumentEngine invariant;
- line-art undo remains available only through drawing/editor paths, not coloring UI.

## Vertical-slice tools

P3.4 child workspace includes:
- child-safe palette;
- one real freehand coloring tool using the existing low-latency Ink surface;
- coloring-only eraser;
- age-appropriate size selection;
- coloring-only Undo / Redo;
- Save & leave / Finish;
- lifecycle autosave/recovery.

A fill tool is required by public V1 only where prepared closed regions are technically valid. Current Cute Cat revision 1 declares no coloring-region asset and currently has coloring disabled, so P3.4 must not fake region fill. The architecture remains compatible with a later persisted `SetRegionFill` operation and authored regions.

## Cute Cat content

P3.4 may enable coloring for Cute Cat revision 1 without fabricating region data:
- `coloring.enabled = true`;
- default mode may be self-coloring;
- vertical-slice authored coloring metadata may contain a simple guidance step if required by schema;
- no fake closed-region IDs;
- Color With Me provides semantic coaching/palette guidance, while the child remains the author of actual color marks.

## Resume

Coloring session persistence must survive:
- Activity recreation;
- app background/foreground;
- process/app relaunch.

Home must surface Continue Coloring when an active coloring session exists. Continue Coloring outranks starting a new lesson for the same working document.

The coloring semantic store and drawing store are separate responsibilities:
- DrawingDocument store owns operations;
- Coloring session store owns mode/phase/default presentation semantics.

## Isolation and safety invariants

Always true:
- teacher/guide operations in child history remain zero;
- color eraser cannot clear protected line art;
- coloring init failure cannot delete/replace the drawing;
- the original editable line-art operations remain present after coloring save/reload;
- UI cannot manufacture a coloring session without a valid completed child document;
- no network/account/analytics/new permissions;
- core path remains fully offline.

## Automated acceptance

At minimum cover:
- schema-1 document decode compatibility;
- schema-2 color operation round-trip;
- color stroke persistence;
- color erase role protection;
- coloring-only undo stops at line art;
- coloring redo restores only color ops;
- layered projection orders color under line art;
- successful Lesson→Coloring handoff;
- initialization failure returns Lesson Engine to post-drawing choice;
- coloring session save/reload/recovery;
- active coloring projection creates Continue Coloring;
- all existing Drawing/Lesson/Quality suites remain green.

## Merge gate

P3.4 is mergeable only when exact-head Android CI is green for:
- unit tests;
- lint;
- debug APK;
- instrumentation APK compile;
- profile APK;
- permission allowlist;
- existing Drawing Engine Ink boundary and Lesson Engine suites.
