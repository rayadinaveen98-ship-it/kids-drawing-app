# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Product
Android-first children's drawing/art-learning app built as a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development model: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

- Phase 0 — COMPLETE
- Phase 1 / Drawing Engine 0.1 — COMPLETE and frozen
- Phase 2 / Lesson Engine 0.2 — COMPLETE and frozen
- Phase 3 / First Vertical Slice 0.3 — COMPLETE
- Phase 4 / Content & Studio Expansion 0.4 — ACTIVE
- P4.1 — COMPLETE
- P4.2 — COMPLETE
- P4.3 — implementation merged; issue #60 remains open for deferred broader physical/content matrix
- P4.4 — COMPLETE; issue #61 closed, PR #69 merged
- Current implementation slice: **P4.5 Coloring V1 Expansion (#62 / draft PR #70)**
- Active branch: `phase4/p4-5-coloring-expansion`
- P4.5 QA candidate: **not frozen yet**; next distributed versionCode must be >16

## Latest fully verified Android product milestone

`0.3.0-vertical-slice` / versionCode 13:
- executable `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`;
- Android CI #307 / run `34706767213` GREEN;
- profile artifact `10301836886`;
- SHA-256 `2f622c116813c7a5830109994c104d4bb1623dc41f57cb4f463c4d6e5e385961`;
- physical 41/41 PASS.

Only repository tag `v0.1.0-art-lab` is known to exist. Do not claim 0.2/0.3 tags exist unless verified later.

## Phase 4 integration state

### P4.3
PR #67 merged at `99767b71a8e4ea20b6d587e4951c822a386019e5`; merged-main CI #342 GREEN. Smiling Sun, Friendly Owl, Simple Rocket and Easy Flower are integrated. Concrete blocking preview/cumulative-guidance defects were focused-retested before integration, but `docs/10-execution/P4_3_CONTENT_QA.md` was not fully rerun. Issue #60 stays open until that matrix is genuinely completed, no later than P4.7.

### P4.4
Issue #61 CLOSED; PR #69 squash-merged at `46c5954fd829e8f64cb752a58c732e895b8e3855`; merged-main CI #379 / run `34754593732` GREEN.

QA1 evidence: `0.4.0-content-studio-p4.4-qa1`, versionCode 16, executable `e24033862b4a653edbbc9f722b4c1560bf96a925`, profile artifact `10315674512`, SHA-256 `cae971fd3e084ac45566ef4f5098741209b07575391c7f3cc989e30e41a4db43`. User physical smoke was positive; unperformed exhaustive rows were not fabricated and remain for P4.7 regression.

## P4.5 active implementation

Issue #62 / draft PR #70. Contract: `docs/10-execution/P4_5_EXECUTION_CONTRACT.md`.

Implemented on the branch so far:
- content API 2 prepared-region model/loader/validation while content API 1 remains compatible;
- empty `regionIds` remains the legacy/freehand contract used by Cute Cat r1;
- prepared region validation checks IDs, bounds, distinct points, nonzero area, self-intersection and missing references;
- DrawingDocument schema 3 adds reversible prepared-region fill history while schema 1/2 stay readable;
- fill operation snapshots region ID, color and polygon geometry;
- coloring Undo/Redo includes fills but cannot cross protected line-art history;
- color raster renders fills; protected line-art raster excludes fills;
- Gallery preview renderer reproduces fills beneath line art;
- `ColoringSessionTool.FILL` added append-only after Brush/Eraser so old ordinals remain stable;
- `PreparedColoringRegionEngine` owns pure hit testing + guided progress derived from authored coloring steps and authoritative document operations;
- `ProductColoringRuntime` restricts guided Fill to current authored region IDs and persists every fill;
- child-facing coloring presentation is now driven by authored progress, replacing hard-coded Cute Cat/fake 3-step stroke-count copy;
- Fill is implemented as a Compose tap overlay mapped with the shared `DocumentViewportMapper`, intentionally leaving the frozen AndroidX Ink Brush/Eraser path untouched;
- production release lesson `little-fish` r1 added through normal catalog discovery: Little/Creative, Draw With Me, first-shapes journey, two guided coloring steps and prepared body/tail/fin regions;
- production catalog tests protect all P4.3 lessons, Cute Cat legacy freehand, and Little Fish prepared-coloring references.

Important: P4.5 is still IMPLEMENTATION ACTIVE, not QA-complete. PR #70 must remain draft/unmerged.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons are structured content, never lesson-ID-specific tutorial code;
- UI cannot set arbitrary engine/session state;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help overlays never become child artwork;
- coloring is structurally below protected line art;
- persistence stores editable operations, not screenshots;
- core drawing/teaching remains offline-first;
- no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions in core milestones;
- critical-path development stays ₹0 where a professional free option exists;
- CI green is required before a slice is complete.

## Immediate continuation

1. Let one full exact current-head Android CI finish; fix compile/unit/content/lint regressions before more scope.
2. Complete prepared-fill runtime/product regression tests including guided multi-region progression, recolor + Undo/Redo, recovery, Cute Cat freehand compatibility and Gallery preview.
3. Freeze `0.4.0-content-studio-p4.5-qa1` with monotonic versionCode 17 unless repository evidence requires higher.
4. Update workflow artifact/evidence names from P4.4 to P4.5.
5. Require exact-head CI green, capture profile artifact/APK size/SHA and write the P4.5 QA record.
6. Physical QA: Little Fish Color With Me + Color Myself; correct Fill hit-testing; body then tail+fin progression; recolor/Undo/Redo; Brush/Eraser coexistence; save/recover/process restart; Gallery preview/reopen; no line-art damage; Cute Cat regression.
7. Only after physical acceptance: mark PR #70 ready, merge, verify merged-main CI, close #62, then begin P4.6.

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. Phase 4 epic #57
5. issue #62 + draft PR #70 + `P4_5_EXECUTION_CONTRACT.md`
6. current branch CI / PR changed files
7. still-open P4.3 issue #60 / `P4_3_CONTENT_QA.md`
8. relevant Drawing/Coloring/Lesson specs.

Do not redesign proven Drawing/Lesson engine foundations merely because a chat changes.
