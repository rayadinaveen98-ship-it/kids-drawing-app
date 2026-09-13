# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning application that behaves like a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development model: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

- Phase 0 — COMPLETE
- Phase 1 / Drawing Engine 0.1 — COMPLETE and frozen
- Phase 2 / Lesson Engine 0.2 — COMPLETE and frozen
- Phase 3 / First Vertical Slice 0.3 — COMPLETE
- Phase 4 / Content & Studio Expansion 0.4 — ACTIVE
- Current implementation slice: **P4.4 Free Draw Studio V1 Core (#61 / draft PR #69)**
- Active branch: `phase4/p4-4-free-draw-studio`
- Current QA identity: `0.4.0-content-studio-p4.4-qa1` / versionCode 16

## Latest fully verified Android product milestone

`0.3.0-vertical-slice` / versionCode 13.

Final executable candidate:
- commit `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`;
- Android CI #307 / run `34706767213` GREEN;
- profile artifact ID `10301836886`;
- APK size `16,080,880` bytes;
- SHA-256 `2f622c116813c7a5830109994c104d4bb1623dc41f57cb4f463c4d6e5e385961`;
- physical QA: 41/41 PASS;
- P3.6 merge commit `7aa53375ed4e0c248335ed26d93bc8cace72bdf2`.

Only repository tag `v0.1.0-art-lab` is known to exist. Do not claim `v0.2.0-lesson-engine` or `v0.3.0-vertical-slice` tags exist unless later created and verified.

## Phase 4 state

Epic #57 targets `0.4.0-content-studio`.

### P4.1 — COMPLETE
Issue #58 / PR #65 / merge `c271ad8b519ec53d99fa18bf140aaf252a8d95c9`.

Deterministic offline `LessonCatalog`, release-package discovery/validation, metadata queries, diagnostics/isolation and catalog-backed product content boundary.

### P4.2 — COMPLETE
Issue #59 / PR #66 / merge `131eee68fd24dad4a80743e10149a867defb59ce`.

- exact-head Android CI #317 GREEN;
- merged-main Android CI #318 / run `34745140470` GREEN;
- multi-lesson recommendations;
- category and Art Journey discovery;
- stable `(lessonId, revision)` product routing;
- recovery scan across installed lessons;
- coloring > drawing > fresh recommendation precedence;
- Cute Cat r1 retains legacy P3 storage identity; new lessons receive deterministic identities.

### P4.3 — IMPLEMENTATION MERGED, ISSUE #60 STILL OPEN

PR #67 implementation merged to `main` at `99767b71a8e4ea20b6d587e4951c822a386019e5`; merged-main Android CI #342 / run `34751781698` GREEN.

Added:
1. `smiling-sun` — Little/Creative Trace & Learn + Draw With Me;
2. `friendly-owl` — Creative/Growing Draw With Me with full Help Ladder levels 1–5;
3. `simple-rocket` — Creative/Growing Watch Then Draw;
4. `easy-flower` — Little/Creative/Growing grouped multi-stroke demonstrations, with Smiling Sun prerequisite.

Cute Cat revision 1 stayed the compatibility baseline. Concrete blocking P4.3 defects were fixed and focused-retested before integration. The broader device/content matrix in `P4_3_CONTENT_QA.md` was not fully rerun, so issue #60 remains open and that coverage must be executed no later than P4.7. Never state that the full P4.3 physical matrix passed unless that evidence is later recorded.

### P4.4 — ACTIVE

Issue #61 / draft PR #69. Contract: `docs/10-execution/P4_4_EXECUTION_CONTRACT.md`. QA record: `docs/10-execution/P4_4_FREE_DRAW_QA.md`.

Current implementation:
- Home Free Draw route is production, not placeholder;
- no lesson prerequisite or Lesson Engine state;
- stable working `DrawingDocument` ID `free-draw-working-v1`;
- same atomic editable document store used by the product;
- persisted child-facing Pencil, Crayon, Marker and Eraser state;
- child-safe palette and three brush sizes;
- Undo/Redo;
- confirmation-only Clear, with Clear itself represented in operation history and therefore undoable;
- Save & leave / resume plus lifecycle save boundary;
- Free Draw Gallery promotion has explicit `FREE_DRAW` source and no lesson provenance;
- Gallery copies use distinct document identities and reopen/delete through the existing Gallery repository;
- global Gallery browsing no longer depends on a selected lesson;
- Free Draw working document is protected from Gallery deletion;
- after Gallery promotion, a new blank working document is persisted before becoming in-memory truth;
- age adaptation changes only presentation/density;
- bounded scrollable controls preserve useful canvas space on smaller phones;
- P4.4-specific unit/domain tests are present;
- no new network/account/analytics/sensitive permission dependency.

QA candidate identity is versionCode 16 / `0.4.0-content-studio-p4.4-qa1`. PR #69 must stay draft until the exact frozen candidate passes CI and physical/product QA.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons are structured content, not lesson-specific screens/code paths;
- UI cannot set arbitrary session states;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help overlays never become child artwork;
- persistence stores editable operations, not screenshots;
- core drawing/teaching remains offline-first;
- no mandatory child account, advertising, behavioral analytics or sensitive permissions in core milestones;
- critical-path development remains ₹0 where a professional free alternative exists.

## Immediate continuation

1. Inspect PR #69 current exact head and its Android CI.
2. Require all tests/lint/debug/instrumentation/profile/permission gates green.
3. Fetch QA1 artifacts and record exact commit/run/artifact/size/SHA in `P4_4_FREE_DRAW_QA.md`.
4. Physically execute the P4.4 matrix from that record; do not infer device PASS from CI.
5. Fix any Free Draw presentation/input/recovery/Gallery defects and refreeze a new exact candidate if necessary.
6. Only after #61 acceptance: mark PR #69 ready, merge, verify `main` CI, close #61.
7. Move to P4.5. Keep P4.3 #60 open until its remaining physical/content coverage is genuinely completed (at latest during P4.7).

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. Phase 4 epic #57
5. issue #61 + PR #69 + `P4_4_EXECUTION_CONTRACT.md`
6. `P4_4_FREE_DRAW_QA.md`
7. still-open P4.3 issue #60 + `P4_3_CONTENT_QA.md`
8. relevant Drawing/Gallery/visual specifications.

Do not restart or redesign the proven Drawing/Lesson engine foundations merely because a chat changes.
