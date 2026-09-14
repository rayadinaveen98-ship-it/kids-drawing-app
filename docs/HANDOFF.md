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
- Verified starting main: `00c618cb9b16444f77e534431b3ca417e85a3e10`
- Starting merged-main CI: #514 / run `34834247565` — GREEN
- Current production catalog: **14 lessons**
- P5.5 target: **20 lessons**
- Expected first P5.5 QA: `0.5.0-curriculum-expansion-p5.5-qa1`, versionCode **24**
- Latest fully verified product release remains **`0.4.0-content-studio`, versionCode 19** until the full Phase-5 milestone releases.

## P5.4 frozen baseline

Set C delivered Happy Lines, Shape Friends, Rainbow Weather, Tree Through Seasons and Ice Cream Shop.

Accepted QA1:
- versionCode 23;
- exact app/content QA commit `797d2219c4fe7f643d31f1ada42e08bacf7d105f`;
- profile artifact `10342178179`;
- profile APK 16,267,569 bytes;
- SHA-256 `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`;
- quality report 14 lessons / 0 errors / 3 reviewed standalone warnings;
- Content Lab 15/15 PASS;
- physical 30/30 PASS; device/API were not provided and not inferred;
- final acceptance CI #513 GREEN;
- merged-main CI #514 GREEN.

ADR-008 remains the generic Trace/open-authorship compatibility rule. Do not add lesson-specific Trace exceptions in later curriculum slices.

## P5.5 locked Set D

Execution contract: `docs/10-execution/P5_5_EXECUTION_CONTRACT.md`  
First contract commit: `d9be4436a77d7a1b967c44cebf7e59182fb6c56f`  
The contract was committed before content implementation.

Locked lessons:
1. **Snail Garden r1** — Little + Creative; difficulty 2; Animal Artist journey; Draw With Me + Watch Then Draw; open shell/garden variation.
2. **Elephant From Shapes r1** — Creative + Growing + Young; difficulty 3; Animal Artist journey; construction/proportion/overlap; no Trace.
3. **Simple Car r1** — Creative + Growing; difficulty 2; standalone vehicle construction; open car design.
4. **Sailboat Scene r1** — Growing + Young; difficulty 3; standalone subject + environment composition; Watch Then Draw + Draw With Me; no Trace.
5. **Planet With Rings r1** — Creative + Growing; difficulty 2; Space Artist journey entry; overlap/centering/palette-choice authorship.
6. **Friendly Alien r1** — Creative + Growing + Young; difficulty 3; Space Artist journey; silhouette/symmetry/story/personality variation.

### Journey contract

`journey.animal_artist`:
Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait.

`journey.space_artist`:
Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.

Simple Car and Sailboat Scene remain intentionally standalone in P5.5; do not invent a journey to clear analyzer warnings.

### Prerequisite contract

- Snail Garden → `little-fish`
- Elephant From Shapes → `friendly-owl`
- Simple Car → `shape-friends`
- Sailboat Scene → `simple-car`
- Planet With Rings → none
- Friendly Alien → `simple-rocket`

### Coverage checkpoint after Set D

Expected catalog support:
- Little: 8
- Creative: 18
- Growing: 14
- Young: 6

P5.6 is responsible for the final Young Artist growth to the Phase-5 floor; do not mark easier Set-D content all-ages merely to inflate coverage.

### Quality-warning contract

The final 20-lesson catalog may contain exactly five reviewed `NO_JOURNEY_MEMBERSHIP` warnings:
- Rainbow Weather
- Tree Through Seasons
- Ice Cream Shop
- Simple Car
- Sailboat Scene

No other warning/error is accepted. The analyzer must remain strict; do not invent journeys or suppress diagnostics globally.

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

1. Require P5.5 contract + continuation-doc exact-head CI green.
2. Implement **Animal Batch A only**: Snail Garden + Elephant From Shapes.
3. Add package/journey/prerequisite/open-authorship/geometry/Companion tests and move the quality checkpoint 14 → 16.
4. Run exact-head CI before Vehicle/Scene Batch B.
5. Implement Simple Car + Sailboat Scene, then Planet With Rings + Friendly Alien as separate gated batches.
6. Reach the 20-lesson quality/coverage gate, inspect all six in Content Lab, freeze QA1 versionCode 24, capture exact APK evidence, physically accept, then merge only after final acceptance CI.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. epic #73
5. issue #82 / PR #83
6. `docs/10-execution/P5_5_EXECUTION_CONTRACT.md`
7. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
8. P5.2 content-production/QA records when tooling evidence is needed
9. P5.4 QA + ADR-008 only when the frozen Trace/open-authorship behavior matters.

Do not reopen proven foundations merely because a chat changes.
