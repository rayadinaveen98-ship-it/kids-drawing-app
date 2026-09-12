# Renderer Raster Cache Plan — P1.7 Performance Fix

## Problem

Physical Class-M testing on Samsung SM-A546E exposed two related failures:

1. W1 continuous drawing spends too much time replaying already-committed vector operations every visible frame.
2. W2 visible Undo/Redo still exceeds the 50 ms P95 gate even after caching expensive Ink stroke rehydration.

The editable vector document remains correct and must remain authoritative. The fix is a projection optimization, not a document-format change.

## Locked approach

Use a 1000x1000 transparent committed-art raster cache matching document coordinates.

- Child/lesson document operations remain vector/semantic source of truth.
- Wet Ink remains AndroidX Ink low-latency authoring.
- Committed operations are flattened into an internal bitmap projection.
- Normal frames draw one committed bitmap plus the wet Ink layer instead of replaying hundreds/thousands of committed vectors.
- Erase masks apply with CLEAR semantics while rasterizing; they are never white paint.
- Clear resets only the projection; the document history remains authoritative.

## Undo/Redo checkpoints

Keep a bounded set of recent raster checkpoints.

Initial values:
- checkpoint interval: 8 operations;
- recent history window: 48 operations;
- maximum retained checkpoints: 8.

A recent Undo/Redo restores the nearest prior checkpoint and replays at most a small tail. Deep-history operations may rebuild more work, but the normal single-stroke W2 benchmark must meet the Class-M <=50 ms P95 gate.

Approximate cache pixel cost at the fixed 1000x1000 logical document size is 4 MiB per ARGB checkpoint. Eight checkpoints therefore cap checkpoint pixels near 32 MiB before object overhead. This must be included in soak/memory validation and may be tuned downward if retained-heap evidence requires it.

## Correctness constraints

- Raster cache must never become persistence truth.
- Save/load continues to use vector operations.
- Reloading/recovering reconstructs the raster cache from the authoritative document.
- Projection ordering must exactly preserve Ink, erase-mask and Clear ordering.
- Undo Clear must restore pre-clear visual content exactly.
- New edits after Undo invalidate future-branch checkpoints.
- Switching document IDs invalidates all renderer projection caches.

## Acceptance

Before this optimization is accepted:
- automated operation ordering/branch/Clear cache tests are green;
- CI/lint/privacy/Ink-boundary checks are green;
- physical W1 >16.7 ms frame rate <=3%, P95 <=16.7 ms, P99 <=33.4 ms on Class M;
- W2 visible Undo/Redo x20 P95 <=50 ms on Class M;
- W3 remains openable/editable without OOM;
- 30-minute soak shows no unbounded cache growth.
