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
- Current slice: **P4.3 Representative Content Set A (#60)**
- Active branch: `phase4/p4-3-representative-content-a`

## Latest verified Android product milestone

`0.3.0-vertical-slice` / versionCode 13.

Final executable candidate:
- commit `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`;
- Android CI #307 / run `34706767213` GREEN;
- profile artifact ID `10301836886`;
- APK size `16,080,880` bytes;
- SHA-256 `2f622c116813c7a5830109994c104d4bb1623dc41f57cb4f463c4d6e5e385961`;
- physical QA: 41/41 PASS;
- P3.6 merge commit `7aa53375ed4e0c248335ed26d93bc8cace72bdf2`.

Evidence: `docs/10-execution/P3_6_PHYSICAL_QA.md` and `docs/10-execution/P3_6_RELEASE_REPORT.md`.

Only repository tag `v0.1.0-art-lab` currently exists. Do not claim `v0.2.0-lesson-engine` or `v0.3.0-vertical-slice` tags exist unless they are later created and verified.

## Phase 4 state

Epic #57 targets `0.4.0-content-studio`.

### P4.1 — COMPLETE
Issue #58 / PR #65 / merge `c271ad8b519ec53d99fa18bf140aaf252a8d95c9`.

Built the deterministic offline `LessonCatalog`, release-package discovery/validation, metadata queries, diagnostics/isolation and catalog-backed product content boundary.

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

### P4.3 — ACTIVE
Issue #60. Execution contract: `docs/10-execution/P4_3_EXECUTION_CONTRACT.md`.

Four new production packages are the intended Set A:
1. `smiling-sun` — Little/Creative Trace & Learn + Draw With Me;
2. `friendly-owl` — Creative/Growing Draw With Me with full Help Ladder levels 1–5;
3. `simple-rocket` — Creative/Growing Watch Then Draw;
4. `easy-flower` — Little/Creative/Growing grouped multi-stroke demonstrations, with Smiling Sun prerequisite.

Cute Cat revision 1 must remain unchanged in P4.3.

P4.3 QA record: `docs/10-execution/P4_3_CONTENT_QA.md`. Do not claim physical P4.3 usability until the device checks in that record are actually executed and recorded.

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

1. Inspect P4.3 branch/PR and exact-head CI.
2. Resolve any loader/catalog/test/Android regression before considering merge.
3. Execute and record P4.3 product/device content checks from `P4_3_CONTENT_QA.md`.
4. Merge only after #60 acceptance; verify merged-main CI; close #60.
5. Start P4.4 Free Draw Studio V1 Core (#61).

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. Phase 4 epic #57
5. current active slice issue/PR/contract (currently #60 / P4.3)
6. `docs/05_CONTENT_ARCHITECTURE.md`
7. `docs/16_LESSON_PACKAGE_AND_AUTHORING.md`
8. `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`
9. relevant Lesson/Coloring/Visual specs

Do not restart or redesign the proven Drawing/Lesson engine foundations simply because a chat changes.
