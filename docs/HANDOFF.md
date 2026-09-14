# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning app built as a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

## Current state

- Phase 0 — COMPLETE
- Phase 1 / Drawing Engine 0.1 — COMPLETE and frozen
- Phase 2 / Lesson Engine 0.2 — COMPLETE and frozen
- Phase 3 / First Vertical Slice 0.3 — COMPLETE
- Phase 4 / Content & Studio Expansion 0.4 — COMPLETE and frozen
- Phase 5 / Curriculum & Teaching Experience Expansion 0.5 — ACTIVE
- Parent epic: #73
- P5.1 #74 — COMPLETE; PR #75 merged; merged-main CI #452 GREEN
- P5.2 #76 — COMPLETE; PR #77 merged; merged-main CI #482 GREEN
- P5.3 #78 — COMPLETE; PR #79 squash-merged at `e3553414c591ae5def3d9016c1a63e9d1a350f39`; merged-main CI #498 GREEN; physical matrix 20/20 PASS
- Current slice: **P5.4 Curriculum Expansion Set C #80**
- Draft PR: #81
- Active branch: `phase5/p5-4-curriculum-set-c`
- Latest fully verified product release remains **`0.4.0-content-studio`, versionCode 19**
- Current QA candidate: **`0.5.0-curriculum-expansion-p5.4-qa1`, versionCode 23**
- P5.4 automated QA: **PASS**
- P5.4 interactive Content Lab inspection: **PENDING**
- P5.4 physical-device acceptance: **PENDING**

## P5.4 exact automated QA candidate

- exact QA commit: `797d2219c4fe7f643d31f1ada42e08bacf7d105f`
- complete Set-C CI #508 / run `34830695250` — GREEN
- exact frozen QA1 CI #509 / run `34831113980` — GREEN
- profile artifact `10342178179`
- profile APK `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.4_QA1-profile.apk`
- profile APK size `16,267,569 bytes`
- profile APK SHA-256 `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`
- debug artifact `10341973952`
- debug APK size `20,480,506 bytes`
- debug APK SHA-256 `c47aa4167716ceb9123b683f23c546fb6460d5160ac80e060188db4038360e9d`
- content-quality artifact `10342606597`
- report `14 lessons / 0 errors / 3 reviewed warnings`
- reviewed warning code is only `NO_JOURNEY_MEMBERSHIP`
- reviewed standalone lesson IDs are exactly `rainbow-weather`, `tree-through-seasons`, `ice-cream-shop`
- permission allowlist passed for both debug and profile APKs

## P5.4 delivered Set C

1. **Happy Lines r1** — Little + Creative, difficulty 1, Draw With Me + selected Trace, open final marks turn.
2. **Shape Friends r1** — Little + Creative, difficulty 1, prerequisite Happy Lines, Draw With Me + selected Trace, combined-shape construction + open final friend turn.
3. **Rainbow Weather r1** — Little + Creative, difficulty 2, prerequisite Smiling Sun, Draw With Me, exactly three large prepared coloring regions + open weather details.
4. **Tree Through Seasons r1** — Creative + Growing + Young, difficulty 3, prerequisite Easy Flower, Draw With Me + Watch Then Draw, no Trace Help, open seasonal/story variation.
5. **Ice Cream Shop r1** — Creative + Growing, difficulty 2, prerequisite Shape Friends, Draw With Me, open topping/sign customization.

Production catalog grew from 9 to **14 release lessons**.

## P5.4 architecture and QA notes

- Existing production `LessonPackageLoader`, `LessonCatalog`, Lesson Engine, Drawing Engine and coloring architecture remain authoritative.
- No second content runtime/parser was introduced.
- No lesson-ID-specific product/runtime UI branches were introduced.
- Companion V2 remains pure/read-only over lesson/session truth and is integration-tested against all five Set-C packages.
- Open-authorship turns use `MANUAL_DONE` with empty required expected geometry where intended.
- One real generic Trace contract mismatch was exposed by Set C and resolved by the documented ADR-008 compatibility clarification rather than lesson-specific behavior.
- Rainbow prepared regions are production-validated.
- Tree explicitly supports Watch Then Draw and has no Trace Help.
- Automated Set-C gates are green, but automated validation is not a substitute for interactive Content Lab or physical-device QA.

Authoritative files:
- `docs/10-execution/P5_4_EXECUTION_CONTRACT.md`
- `docs/10-execution/P5_4_CONTRACT_CLARIFICATION_01_TRACE_OPEN_AUTHORSHIP.md`
- `docs/10-execution/P5_4_CONTENT_QA.md`
- `docs/10-execution/P5_4_QA.md`
- `docs/adr/ADR-008-trace-open-authorship.md`

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons remain structured content, never lesson-ID-specific tutorial code;
- `LessonSessionState` remains the only teaching-state truth;
- Companion presentation never mutates Help level, completion, artwork, persistence or scoring;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help/reference overlays never become child artwork;
- coloring/fill remains structurally below protected line art;
- Free Draw remains lesson-independent with explicit Gallery provenance;
- persistence stores editable operations, not screenshots;
- core product remains offline-first;
- no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions;
- engine changes require a concrete defect + explicit contract/ADR.

## Immediate continuation

1. Require exact-head CI green after the QA evidence/continuation-doc synchronization.
2. Perform real interactive Content Lab inspection for all five Set-C lessons.
3. Install exact profile artifact `10342178179` and perform the focused physical P5.4 matrix.
4. Record actual device/API and pass/fail evidence; do not infer it.
5. If any defect changes code/content, cut a new QA candidate/versionCode and repeat automated evidence.
6. If interactive + physical acceptance pass, run final acceptance-doc CI, mark PR #81 ready, squash-merge, verify merged-main CI, then close #80 completed.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. epic #73
5. issue #80 / PR #81
6. `docs/10-execution/P5_4_EXECUTION_CONTRACT.md`
7. `docs/10-execution/P5_4_QA.md`
8. `docs/10-execution/P5_4_CONTENT_QA.md`
9. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
10. P5.2 QA record when tooling evidence is needed.

Do not reopen proven foundations merely because a chat changes.
