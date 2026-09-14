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
- P5.3 #78 — COMPLETE; PR #79 merged; merged-main CI #498 GREEN; physical 20/20 PASS
- P5.4 #80 — COMPLETE; PR #81 squash-merged at `00c618cb9b16444f77e534431b3ca417e85a3e10`; Content Lab 15/15 PASS; physical 30/30 PASS; merged-main CI #514 GREEN
- Current slice: **P5.5 Curriculum Expansion Set D #82**
- Draft PR: #83
- Active branch: `phase5/p5-5-curriculum-set-d`
- P5.5 implementation: **COMPLETE**
- Automated QA1: **GREEN**
- Interactive acceptance: **PENDING**
- Current production catalog: **20 lessons**
- QA candidate: `0.5.0-curriculum-expansion-p5.5-qa1`, versionCode **24**
- Exact QA app/content commit: `3a538b5f7c2db118a0006176b7093b0e22961f9b`
- Latest fully verified product release remains **`0.4.0-content-studio`, versionCode 19** until the full Phase-5 milestone releases.

## P5.5 delivered Set D

1. **Snail Garden r1** — Little + Creative; difficulty 2; Animal Artist journey; Draw With Me + Watch Then Draw; open shell/garden variation.
2. **Elephant From Shapes r1** — Creative + Growing + Young; difficulty 3; Animal Artist journey; construction/proportion/overlap; no Trace.
3. **Simple Car r1** — Creative + Growing; difficulty 2; standalone vehicle construction; open car design.
4. **Sailboat Scene r1** — Growing + Young; difficulty 3; standalone subject + environment composition; Watch Then Draw + Draw With Me; no Trace.
5. **Planet With Rings r1** — Creative + Growing; difficulty 2; Space Artist journey entry; overlap/centering/palette-choice authorship.
6. **Friendly Alien r1** — Creative + Growing + Young; difficulty 3; Space Artist journey; silhouette/symmetry/story/personality variation.

Catalog grew **14 → 20 release lessons**.

## Journey contract now implemented

`journey.animal_artist`:
Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait.

`journey.space_artist`:
Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.

Simple Car and Sailboat Scene remain intentionally standalone; do not invent a journey merely to clear analyzer warnings.

During P5.5 validation, retained Little Fish/Cute Cat metadata was corrected to canonical P5.1 Animal Artist membership. This was a metadata/test-fixture compatibility correction only; no lesson identity, drawing geometry or saved-artwork contract changed.

## Verified P5.5 QA1 evidence

Android CI #525 / run `34837734724` is GREEN on exact QA app/content commit `3a538b5f7c2db118a0006176b7093b0e22961f9b`.

Quality:
- lessons: **20**;
- errors: **0**;
- warnings: **5**;
- accepted warning code: `NO_JOURNEY_MEMBERSHIP` only;
- accepted warning lessons: Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene.

Coverage:
- Little: **8**;
- Creative: **18**;
- Growing: **14**;
- Young: **6**.

Release-like profile APK:
- artifact ID `10344519403`;
- APK `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.5_QA1-profile.apk`;
- APK size **16,293,898 bytes**;
- SHA-256 `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`.

Debug APK:
- artifact ID `10345315128`;
- APK size **20,508,319 bytes**;
- SHA-256 `79fd638cefa5e1e46a337d2a35a720a1586139a7aa3c8b864b893e880095e307`.

Content-quality artifact: `10344354743`.

Permission allowlist passed for debug/profile APKs. No unexpected sensitive permission was introduced.

## Remaining P5.5 gate

Read `docs/10-execution/P5_5_QA.md` and use the fixed `docs/10-execution/P5_5_FOCUSED_ACCEPTANCE_CHECKLIST.md`.

Required observations:
- Content Lab **18/18 PASS**;
- exact-profile physical matrix **36/36 PASS**;
- no binary/content-changing defect;
- actual device/API recorded when supplied; never infer it.

Automated CI is not a substitute for physical/interactive acceptance. Keep PR #83 **draft** until those checks genuinely pass and final acceptance-doc CI is green.

If a binary/content defect is found, invalidate QA1 and cut a new candidate with a new versionCode; never silently reuse versionCode 24 for changed binaries.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- Lessons remain structured content, never lesson-ID-specific tutorial code.
- `LessonSessionState` remains the only teaching-state truth.
- Companion presentation never mutates Help level, completion, artwork or persistence.
- AndroidX Ink remains behind owned drawing infrastructure.
- teacher/trace/help/reference overlays never become child artwork.
- coloring/fill remains structurally below protected line art.
- Free Draw remains lesson-independent with explicit Gallery provenance.
- persistence stores editable operations, not screenshots.
- core remains offline-first, account-free, ad-free and free of behavioral analytics.
- no similarity scoring, grades, rank, stars/XP, permanent ability labels, punitive streaks or cloud child profiling.
- engine changes require a concrete defect + explicit contract/ADR.

## Immediate continuation

1. Require CI green for the QA evidence/checklist synchronization commit.
2. Run Content Lab 18/18 against the frozen 20-lesson catalog.
3. Install only profile artifact `10344519403` and run the 36-row exact-profile matrix.
4. Record genuine results; do not infer PASS.
5. If 18/18 + 36/36 pass with no binary-changing defect: commit acceptance evidence, run final acceptance-doc CI, mark PR #83 ready, squash-merge, verify merged-main CI, close #82.
6. Start P5.6 only from verified post-P5.5 `main`.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. epic #73
5. issue #82 / PR #83
6. `docs/10-execution/P5_5_QA.md`
7. `docs/10-execution/P5_5_FOCUSED_ACCEPTANCE_CHECKLIST.md`
8. `docs/10-execution/P5_5_EXECUTION_CONTRACT.md`
9. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
10. P5.2 tooling and P5.4/ADR-008 only when those frozen behaviors are relevant.

Do not reopen proven foundations merely because a chat changes.
