# P4.4 — Free Draw Studio V1 Core — Execution Contract

**Issue:** #61  
**Parent:** #57  
**Base:** P4.3 merge `99767b71a8e4ea20b6d587e4951c822a386019e5`  
**Status:** active

## Objective

Replace the Home Free Draw placeholder with a production, offline creative studio built on the already-verified Drawing Engine, persistence and Gallery foundations. Free Draw must not create a second drawing engine, must not depend on Lesson Engine state, and must preserve editable `DrawingDocument` operations as artwork truth.

## Frozen invariants

1. `DrawingDocument` + `DrawingDocumentEngine` remain authoritative artwork/history owners.
2. AndroidX Ink remains contained behind drawing infrastructure.
3. Only child-authored operations enter the Free Draw document.
4. Persistence stores editable operations, never canvas screenshots.
5. Free Draw requires no lesson/session/teacher runtime.
6. Network, account, analytics and sensitive permissions remain absent.
7. Existing lesson/coloring behavior is regression scope and must stay green.
8. Gallery promotion creates an independent durable Gallery document and never aliases the working canvas.

## Working artwork model

- One stable local working document ID is used for resumable Free Draw V1.
- Its `DrawingDocumentMetadata` has no lesson ID/revision.
- Opening Free Draw recovers the durable working document when present; otherwise it creates a blank 1000×1000 paper document.
- Child stroke, erase, undo, redo and clear mutations remain operation-based.
- Save & leave persists the working document and tool state before returning Home.
- Process/lifecycle recovery restores both artwork and the last safe tool presentation.
- A successful Gallery finish promotes an independent copy with `GalleryArtworkSource.FREE_DRAW`; the working canvas is then reset to a fresh blank document so a finished snapshot is not offered as an in-progress resume.

## Tool model

P4.4 extends product-owned drawing settings with a brush preset while keeping the existing drawing/erase semantic split.

Required child-facing presets:
- Pencil — pressure-pen family, opaque, fine default;
- Crayon — marker-family stroke with a softer semi-opaque rendering profile;
- Marker — marker-family stroke, opaque, broader default;
- Eraser — existing erase-mask semantics.

Every committed ink stroke persists a stable `brushPresetId`, color, opacity and base size so reload/reopen reproduces the selected visual tool. Unsupported or unknown historical preset IDs retain the existing safe fallback behavior.

No new document schema is required for P4.4.

## Palette and size

- fixed child-safe local palette; no unrestricted color picker;
- compact set of named size choices backed by existing numeric width limits;
- age bands change control density/presentation only, never artwork semantics;
- Little Artist controls are larger and less dense;
- older bands may expose the same capabilities more compactly.

## Editing controls

- Undo and Redo call the authoritative `DrawingDocumentEngine` and immediately reconcile the surface.
- Clear must require explicit confirmation at both product flow and runtime boundary; accidental one-tap clear is not allowed.
- Eraser remains an operation and participates in the same editable history.

## Gallery integration

Gallery promotion must carry explicit source provenance instead of inferring all artwork as lesson work.

Required rules:
- lesson/coloring completion => `GalleryArtworkSource.LESSON`;
- Free Draw completion => `GalleryArtworkSource.FREE_DRAW`;
- lesson source requires lesson ID + revision provenance;
- Free Draw source carries no lesson ID/revision;
- shared Gallery list/reopen/delete/preview behavior must work for both sources;
- deleting a Gallery copy can never delete the current working Free Draw document.

Product Gallery access must no longer require a current lesson runtime merely to list/reopen/delete artwork.

## Home / routing

- Existing `StudioDestination.FREE_DRAW` is retained.
- The current placeholder route is replaced by the production Free Draw workspace.
- Home may indicate resumable Free Draw artwork when the working document contains active child operations.
- Entering Free Draw has no lesson prerequisite.

## Lifecycle

At minimum persist on:
- committed stroke;
- committed erase mask;
- undo/redo;
- confirmed clear;
- explicit Save & leave;
- `ON_STOP` / background boundary.

Tool settings must survive expected lifecycle/process recreation independently of document mutation history.

## Automated acceptance

Tests must prove at minimum:
1. blank creation and recovery use stable Free Draw identity and null lesson provenance;
2. stroke/erase/undo/redo/clear remain editable document operations;
3. clear rejects unconfirmed requests;
4. Pencil/Crayon/Marker produce distinct persisted preset IDs/rendering profiles and survive rehydration;
5. tool settings persistence/recovery;
6. Free Draw Gallery promotion writes `source=FREE_DRAW` and no lesson provenance;
7. existing lesson/coloring Gallery promotion remains `source=LESSON`;
8. Free Draw Gallery copy reopens/deletes safely without touching the working document;
9. age presentation policies expose appropriate density without changing underlying capabilities;
10. existing lesson/coloring/drawing regression suite remains green.

## Physical/product acceptance

Before merge, verify on Android hardware:
- Home → Free Draw opens a real blank/recovered canvas;
- Pencil, Crayon, Marker and Eraser are visibly/functionally distinct;
- color and size controls work at representative age bands;
- Undo/Redo work after mixed strokes/eraser actions;
- Clear always asks for confirmation and Cancel preserves artwork;
- Save & leave → Home → Free Draw resumes correctly;
- background/process recreation preserves artwork and last tool choice;
- Finish/Save to Gallery creates a Free Draw entry with a usable preview/fallback;
- Gallery reopen shows the correct artwork;
- Gallery delete removes only the promoted copy;
- after successful finish, a fresh Free Draw canvas is available;
- lesson start/resume and existing Cute Cat coloring still work;
- airplane mode does not affect the flow.

Record exact commit, device/API, APK size/SHA and PASS/FAIL results before merge.

## Non-goals

- layers, selections, transforms or custom brush editor;
- arbitrary color picker;
- cloud sync/accounts;
- public sharing;
- AI drawing generation;
- prepared-region coloring (P4.5);
- reopening frozen Drawing/Lesson architecture without a concrete defect.
