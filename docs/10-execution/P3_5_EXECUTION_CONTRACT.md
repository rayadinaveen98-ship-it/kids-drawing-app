# P3.5 — Completion Flow + Local Gallery Execution Contract

**Issue:** #47  
**Parent:** #42  
**Depends on:** P3.4 / #46  
**Status:** implementation contract

## Objective

Complete the first child ownership loop after drawing/coloring without weakening DrawingDocument authority: durable completion → calm artwork celebration → local Gallery → exact persisted artwork reopen.

P3.5 is intentionally local/offline. Preview images are derivatives only. Gallery metadata must never become a substitute for the editable DrawingDocument.

## Completion ordering

The product completion coordinator owns sequencing; feature UI only invokes it and presents typed results.

For both **Finish for now** after drawing and **Finish coloring**:

1. finish/flush any accepted child operation;
2. persist the current authoritative working DrawingDocument successfully;
3. finish the relevant Lesson/Coloring semantic state at its valid boundary;
4. promote the finished artwork into a stable Gallery DrawingDocument identity;
5. persist that Gallery DrawingDocument successfully;
6. only then create/update Gallery metadata referencing that exact Gallery document ID;
7. attempt derived preview generation after Gallery truth exists;
8. preview success may update the metadata preview reference;
9. preview failure leaves the Gallery record visible with a product fallback;
10. only after durable artwork + Gallery metadata success may completion celebration be shown.

No completion screen may imply saved ownership before steps 1–6 succeed.

## Stable artwork identity

The physically verified Lesson Engine uses a fixed working document identity for the Cute Cat vertical slice. P3.5 must not reopen or destabilize that verified engine merely to support multiple completed artworks.

Therefore completion performs a **promotion**:

- the working document is first durably saved under its existing session identity;
- a new stable Gallery document ID is generated;
- the completed DrawingDocument is copied as an editable operation document with the new ID;
- logical size, ordered operations, background role and lesson provenance are preserved exactly;
- operation IDs and child-authored geometry remain unchanged;
- the promoted Gallery document is independently persisted through AtomicDrawingDocumentStore;
- Gallery metadata references only the promoted stable document ID.

After promotion, that persisted Gallery DrawingDocument is the authoritative artwork for the Gallery entry. A later lesson may safely reuse/replace its working document without mutating historical Gallery artwork.

Reopen always loads the exact promoted DrawingDocument referenced by Gallery metadata; it never reconstructs artwork from a preview image.

## Gallery metadata

A narrow local Gallery record contains presentation/index data only:

- stable gallery entry ID;
- authoritative Gallery `documentId`;
- title;
- source (`LESSON` for this vertical slice; architecture remains compatible with Free Draw);
- lesson ID/revision when available;
- completion kind (`DRAWING_ONLY` or `COLORED`);
- completed/saved timestamp;
- optional derived preview reference/status.

Gallery metadata must not contain stroke geometry or a flattened artwork payload.

Records are ordered newest-first deterministically.

## Gallery persistence

Use an owned atomic, checksummed local catalog/store.

Rules:
- a Gallery record cannot be inserted until its referenced Gallery DrawingDocument save succeeds;
- catalog writes use temp → sync → atomic replacement/backup semantics consistent with existing stores;
- one corrupt/absent preview cannot make the catalog or authoritative document disappear;
- catalog load failure is contained to Gallery presentation and must not delete DrawingDocuments;
- all core behavior is offline.

## Derived previews

P3.5 generates a small local preview as non-authoritative derived data when possible.

Rules:
- preview generation runs after authoritative artwork + Gallery metadata exist;
- preview generation failure is non-fatal;
- Gallery must show a calm fallback art card when preview is missing/corrupt;
- preview is never used for reopen/edit truth;
- deleting/regenerating a preview cannot mutate the DrawingDocument;
- preview composition respects persisted role ordering: color under protected line art.

No screenshot capture of UI chrome becomes artwork truth.

## Completion presentation

After successful durable completion:
- show the finished artwork as the visual hero;
- use a calm `CELEBRATING_ARTWORK`-style presentation;
- no score, percentage, stars, streak pressure, leaderboard, ranking or exaggerated correctness claims;
- offer clear child-facing actions such as **See in My Gallery** and **Back to studio**;
- reduced-motion compatibility is preserved by keeping celebration non-essential to progression.

A completion-presentation failure must not roll back saved artwork.

## Gallery UI

The Gallery is a visual art wall, not a data table.

Vertical slice requirements:
- newest-first artwork cards;
- derived preview when available, fallback illustration/card when not;
- title and simple date/context;
- tap artwork → detail/reopen;
- detail resolves and renders the exact persisted editable DrawingDocument;
- no outward share/export button in child Gallery for P3.5;
- no likes, scores, ranking or social metrics.

## Reopen

Reopen flow:
1. resolve Gallery record;
2. load `documentId` from AtomicDrawingDocumentStore;
3. validate record/document provenance where present;
4. render the loaded DrawingDocument directly;
5. if preview is missing, reopen still succeeds;
6. if the authoritative document is missing/corrupt, show a contained artwork-safe error; never fabricate from preview.

P3.5 detail view may be read-only presentation. The persisted representation remains fully editable and compatible with a later Gallery editing flow.

## Safe deletion

Destructive deletion is explicit and confirmed.

Child-facing flow:
1. child requests Delete from artwork detail;
2. app shows a clear confirmation with **Keep artwork** as the safe escape;
3. no deletion occurs before explicit confirmation;
4. confirmed deletion removes Gallery metadata and the promoted Gallery DrawingDocument; derived preview cleanup is best-effort;
5. the working lesson document/session is never deleted through a Gallery record;
6. failure during deletion must not silently leave the catalog claiming a document was deleted when it still needs recovery.

The product deletion coordinator owns this ordering; UI does not delete files directly.

## Completion sources

### Finish for now after drawing
- persist working drawing;
- accept Lesson Engine `FinishForNow` at the valid post-drawing boundary;
- promote the completed line-art document;
- create `DRAWING_ONLY` Gallery record;
- attempt preview;
- show completion presentation.

### Finish coloring
- persist current colored DrawingDocument **before** Coloring Session transitions to finished;
- persist finished Coloring Session snapshot;
- promote the colored document;
- create `COLORED` Gallery record;
- attempt preview;
- show completion presentation.

Save & leave during an active lesson/coloring session remains resumable work and does **not** create a completed Gallery record.

## Isolation and safety invariants

Always true:
- Gallery metadata is not artwork truth;
- previews are not artwork truth;
- teacher/guide overlays never enter promoted child artwork;
- line-art/color operation roles remain unchanged during promotion;
- a Gallery save failure cannot delete the working artwork;
- a preview failure cannot hide a successfully saved Gallery artwork;
- reopening never depends on network access;
- no social metrics or competition;
- no new network/account/analytics permissions;
- outward export/share remains absent from child mode in this slice.

## Automated acceptance

At minimum cover:
- promotion preserves operation order, provenance, role semantics and produces a distinct stable document ID;
- authoritative Gallery document save occurs before metadata insertion;
- simulated document-save failure creates no Gallery record;
- Gallery metadata round-trips across process/store recreation;
- newest-first deterministic ordering;
- missing preview still yields visible Gallery record/fallback presentation model;
- preview generation failure leaves artwork + metadata intact;
- reopen resolves the exact referenced DrawingDocument and operations;
- missing authoritative document produces typed reopen failure rather than preview reconstruction;
- drawing-only completion produces Gallery entry;
- colored completion persists color work before completion and produces Gallery entry;
- Save & leave does not create Gallery completion;
- delete requires confirmed product command and never targets the working lesson document;
- existing Drawing/Lesson/Coloring suites remain green.

## Merge gate

P3.5 is mergeable only when exact-head Android CI is green for:
- unit tests;
- lint;
- debug APK;
- instrumentation APK compile;
- profile APK;
- permission allowlist;
- existing Drawing Engine Ink boundary checks;
- existing Lesson/Coloring integration suites.

Full physical/offline/accessibility/device validation and `0.3.0-vertical-slice` APK release evidence remain P3.6 / #48.