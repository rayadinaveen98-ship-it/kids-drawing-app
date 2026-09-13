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
- Current slice: **P4.5 Coloring V1 Expansion (#62 / PR #70) — focused physical acceptance PASS; merge verification pending**
- Active branch: `phase4/p4-5-coloring-expansion`
- P4.5 QA1: `0.4.0-content-studio-p4.5-qa1`, versionCode 17

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

### P4.5
Issue #62 / PR #70. Contract: `docs/10-execution/P4_5_EXECUTION_CONTRACT.md`. QA: `docs/10-execution/P4_5_COLORING_QA.md`.

Delivered:
- content API 2 prepared-region model/loader/validation while content API 1 remains compatible;
- empty `regionIds` remains the legacy/freehand contract used by Cute Cat r1;
- strict prepared-region geometry/reference validation;
- DrawingDocument schema 3 reversible prepared-region fill history while schema 1/2 stay readable;
- fill operation snapshots region ID, color and polygon geometry;
- coloring Undo/Redo includes fills but cannot cross protected line-art history;
- color raster and Gallery preview render fills beneath protected line art;
- `ColoringSessionTool.FILL` added append-only after Brush/Eraser so old ordinals remain stable;
- pure hit testing + authored guided progress derived from authoritative document operations;
- guided Fill restricted to current authored region IDs and persisted immediately;
- child-facing guidance is authored/content-driven rather than hard-coded Cute Cat/stroke-count copy;
- Fill uses a Compose tap overlay with shared `DocumentViewportMapper`, leaving verified AndroidX Ink Brush/Eraser input untouched;
- release lesson `little-fish` r1 proves body → tail+fin prepared-region coloring through normal catalog discovery.

QA1 evidence:
- executable `240007b6161ebfefd09252efa844e4d18808a7f0`;
- Android CI #415 / run `34756548584` GREEN;
- profile artifact `10317956245`;
- size `16,183,450 bytes`;
- SHA-256 `cf45fedc129523c8c9d3784e3d6ea70d0319dc1b8f9236430b85c099075394d6`;
- user reported the supplied focused P4.5 checklist PASS on 2026-09-13.

Do not overclaim the focused pass. Process-death-specific sequence, exact coloring-undo boundary sequence, airplane mode, and small-screen-specific physical coverage remain deferred to P4.7; automated contracts for those foundations remain green where applicable.

P4.5 implementation is accepted. Remaining gate: exact-head CI on acceptance-documentation head → ready/merge PR #70 → merged-main CI → close #62.

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

1. Wait for exact-head CI on the acceptance-documentation head and fix only concrete failures.
2. Mark PR #70 ready and squash-merge after exact-head green.
3. Verify merged-main Android CI.
4. Close issue #62 completed.
5. Create P4.6 branch from verified main and execute Representative Content Set B + cross-content QA (#63).

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. Phase 4 epic #57
5. issue #62 + PR #70 + `P4_5_EXECUTION_CONTRACT.md` + `P4_5_COLORING_QA.md`
6. current P4.5 exact-head/merged-main CI status
7. still-open P4.3 issue #60 / `P4_3_CONTENT_QA.md`
8. relevant Drawing/Coloring/Lesson specs.

Do not redesign proven Drawing/Lesson engine foundations merely because a chat changes.
