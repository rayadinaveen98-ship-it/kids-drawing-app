# P4.5 — Coloring V1 Expansion Execution Contract

**Issue:** #62  
**Parent epic:** #57  
**Branch:** `phase4/p4-5-coloring-expansion`  
**Base:** P4.4 merge `46c5954fd829e8f64cb752a58c732e895b8e3855` with merged-main CI #379 green  
**Target:** prepared-region fill + content-driven guided coloring without weakening protected line art or legacy freehand compatibility

## 1. Non-negotiable architecture

P4.5 extends the existing Coloring Engine/product runtime; it does not introduce a second paint system.

- `DrawingDocument` remains the only authoritative editable artwork history.
- protected drawing/line-art operations remain structurally separate from coloring operations.
- coloring projection remains below protected line art.
- `ColoringSessionEngine` continues to own coloring session semantics; product UI only renders/dispatches typed actions.
- lesson coloring behavior remains authored content interpreted generically; no lesson-ID-specific coloring UI.
- no bitmap/screenshot flattening may become the source of truth for fill.
- schema-1 and schema-2 drawing documents must remain readable.
- Cute Cat revision 1 freehand coloring must remain compatible without adding fake prepared regions.

## 2. Existing authored contract that P4.5 activates

The current lesson contract already provides:
- optional `assets.coloringRegions`;
- `coloring.enabled` and `defaultMode`;
- ordered coloring steps;
- `regionIds`;
- narration keys;
- suggested semantic color roles;
- optional `enforceSuggestedColors`.

P4.5 must make these fields real end-to-end rather than create a parallel schema.

Legacy/freehand rule:
- an enabled coloring step with empty `regionIds` is a freehand-only step;
- prepared-region fill is available only when non-empty step region references resolve against a valid authored region catalog;
- old Cute Cat r1 remains freehand and does not gain a synthetic region asset.

The machine-readable lesson schema must be corrected to permit empty `regionIds`, matching the already-frozen semantic validator and production Cute Cat package.

## 3. Prepared region asset v1

Use one app-owned JSON vector format referenced by `assets.coloringRegions`.

Conceptual source:

```json
{
  "schemaVersion": "1.0",
  "regions": [
    {
      "id": "roof",
      "points": [
        {"x": 300, "y": 420},
        {"x": 500, "y": 250},
        {"x": 700, "y": 420}
      ]
    }
  ]
}
```

V1 rules:
- IDs follow existing content-ID rules and are unique within the region catalog;
- geometry uses lesson logical coordinates, never pixels;
- at least 3 distinct polygon points;
- polygon is implicitly closed from final point to first point;
- all coordinates finite and inside authored canvas bounds;
- non-zero polygon area;
- no self-intersection;
- no holes/multi-polygons in P4.5 V1;
- every authored coloring `regionId` must resolve;
- release packages that declare prepared region references must provide the asset;
- malformed/open/invalid prepared geometry blocks prepared-fill availability rather than exposing deceptive UI.

The runtime package should carry the validated region catalog so product code does not reopen/reparse arbitrary asset text.

Prepared-region content should require the new content API while old packages remain loadable.

## 4. Reversible fill operation / drawing document schema 3

Add a coloring-only region-fill operation to `DrawingDocument` and increment current drawing document schema to 3.

A fill operation owns:
- stable operation ID/time;
- authored `regionId`;
- selected ARGB color;
- self-contained validated polygon geometry snapshot in logical document coordinates.

Why geometry is snapshotted into the operation:
- Gallery/recovery rendering must not depend on a mutable future version of the lesson package;
- old saved artwork remains visually deterministic even if content is later revised;
- persistence remains editable/vector, never a flattened image.

Rules:
- fill is a coloring operation for coloring-only Undo/Redo boundaries;
- schema 1/2 documents decode unchanged;
- schema 3 codec adds a new operation tag without changing the outer document envelope;
- operation order remains authoritative;
- repeated fills of one region naturally resolve to the latest active fill in timeline order;
- Undo exposes the previous region fill/color when applicable;
- color erase may erase fill projection visually without ever altering line art;
- a later fill may repaint the prepared region.

## 5. Rendering and hit-testing

`CommittedColorRasterCache` remains the coloring projection owner.

- Add region-fill rendering with an Android `Path`/fill `Paint` in document coordinates.
- Render fill operations in timeline order with freehand color strokes and color erase masks.
- Never draw region fills into the protected line-art cache.
- Gallery/recovery reconciliation renders entirely from document operations.

Fill tap behavior must use the validated authored polygons:
- map touch into document coordinates using the existing viewport mapping principles;
- choose only a prepared region containing the tap;
- taps outside valid regions perform no artwork mutation;
- ambiguous overlapping prepared regions are rejected at content-validation time or resolved deterministically by the authored ordering policy; P4.5 content should avoid overlaps.

## 6. Coloring session semantics

Extend session tooling with `FILL` while preserving existing Brush/Eraser compatibility.

- append the enum value rather than reordering old ordinal values;
- selected Fill is allowed only when the active lesson package exposes valid prepared regions;
- legacy/freehand coloring continues to expose Brush/Eraser and no deceptive Fill tool;
- selecting a palette color remains a child choice and can activate the appropriate paint tool without touching lesson drawing tool state.

`COLOR_MYSELF`:
- may freely use Brush/Eraser and, when valid prepared regions exist, Fill;
- may recolor the same region repeatedly.

`COLOR_WITH_ME`:
- current guidance comes from authored `coloring.steps`, not freehand stroke counts;
- current step targets its authored region IDs;
- progress is derived from authoritative active fill operations/step content where possible instead of duplicating artwork truth in the session snapshot;
- narration/suggested-color roles come from content;
- reasonable child color choice stays allowed by default;
- do not fake semantic-color enforcement until an explicit role→palette mapping exists.

Only bump coloring-session snapshot schema if persisted state beyond derivable document/content state is actually required; backward recovery must be explicit.

## 7. Product UI

Remove generic Cute Cat-specific coloring chrome from shared coloring UI.

- shared title/instructions derive from active lesson metadata/coloring state;
- Color With Me shows authored step progress and guidance;
- Color Myself remains concise and non-prescriptive;
- Fill button appears only when prepared fill is genuinely supported;
- Fill interaction must not pass a tap into Ink as a stray brush stroke;
- Brush/Crayon-like freehand, Eraser, palette, size, Undo/Redo continue to work;
- age presentation may adapt density only; artwork/session truth stays engine-owned.

## 8. Production proof content

P4.5 cannot close on fixture-only geometry. Add at least one release lesson/package that safely exposes prepared-region fill through the normal catalog/loader/product path.

Prefer a new lesson/package rather than revising Cute Cat r1 in place. The lesson must:
- have clear closed prepared regions appropriate for touch fill;
- exercise authored Color With Me ordering;
- allow Color Myself;
- use normal catalog metadata/routing;
- be useful later as the guided-region member of the Phase-4 representative set.

## 9. Required automated evidence

At minimum prove:
- region asset decode and strict validation;
- duplicate/missing/open-equivalent/degenerate/out-of-bounds/self-intersecting rejection;
- empty `regionIds` legacy/freehand compatibility;
- missing region references rejected;
- schema-1 and schema-2 drawing documents still decode;
- schema-3 fill document round-trip;
- fill participates in coloring Undo/Redo but cannot cross line-art boundary;
- repeated region fill ordering and undo reveal previous fill;
- fill projection is below protected line art;
- fill + freehand + erase ordering is deterministic;
- lifecycle/process restore preserves fill operations and current coloring semantics;
- Gallery promotion/reopen preserves fill + freehand operations;
- legacy Cute Cat coloring regression remains green;
- invalid/non-authored lessons never expose Fill;
- production prepared-region package loads without diagnostics;
- full existing Android CI, lint, APK, Ink-boundary and permission gates stay green.

## 10. Exit gate

P4.5 closes only when:
1. prepared-region contract is implemented and validated;
2. reversible schema-3 fill operations persist/recover/render correctly;
3. generic Color With Me uses authored region progression;
4. Color Myself supports safe fill when authored while legacy freehand remains valid;
5. protected line art remains immutable;
6. at least one release catalog lesson exercises real prepared fill;
7. exact-head CI is green;
8. a P4.5 QA APK is produced with exact commit/run/artifact/size/SHA evidence;
9. physical/product QA verifies fill targeting, undo/recolor, freehand coexistence, recovery and Gallery reopen without overstating unperformed checks.

P4.6 starts only after this gate or an explicit documented decision to parallelize.