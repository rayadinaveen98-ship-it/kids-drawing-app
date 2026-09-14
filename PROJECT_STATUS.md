# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19  
**Latest completed Phase-5 slice:** P5.3 — Companion / Teacher Experience V2  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE**  
**Active branch:** `phase5/p5-4-curriculum-set-c`  
**Parent epic:** #73  
**Current slice:** P5.4 — Curriculum Expansion Set C #80  
**Draft PR:** #81  
**Current QA candidate:** `0.5.0-curriculum-expansion-p5.4-qa1`, versionCode 23  
**Last updated:** 2026-09-14

Git is authoritative when chat memory and repository state disagree.

## Completed milestones

- Phase 0 — Product Foundation — COMPLETE.
- Phase 1 — Drawing Engine `0.1.0-art-lab`, versionCode 11 — COMPLETE/frozen.
- Phase 2 — Lesson Engine `0.2.0-lesson-engine`, versionCode 12 — COMPLETE/frozen; physical 32/32 PASS.
- Phase 3 — First Vertical Slice `0.3.0-vertical-slice`, versionCode 13 — COMPLETE; physical 41/41 PASS.
- Phase 4 — Content & Studio Expansion `0.4.0-content-studio`, versionCode 19 — COMPLETE/frozen.
- P5.1 — Curriculum & Teaching Contract #74 — COMPLETE; PR #75 squash-merged at `cea06e219290c82b9a1f8f8007069c61841bbc95`; merged-main CI #452 GREEN.
- P5.2 — Content Production System V2 #76 — COMPLETE; PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`; merged-main CI #482 GREEN.
- P5.3 — Companion / Teacher Experience V2 #78 — COMPLETE; focused physical matrix 20/20 PASS; PR #79 squash-merged at `e3553414c591ae5def3d9016c1a63e9d1a350f39`; merged-main CI #498 GREEN; issue #78 closed completed.

No 0.2/0.3/0.4 tag is claimed unless separately verified. Phase 4 remains frozen.

## Phase 5 target locked by P5.1

Target milestone: `0.5.0-curriculum-expansion`.

- 24 total production guided lessons;
- 9 verified Phase-4 lessons retained + 15 purposeful new lessons;
- all four age bands receive real progression;
- guidance decreases and creative authorship increases with age;
- no similarity scoring, permanent ability labels, punitive streaks or cloud profiling;
- structured/generic offline-first content;
- P5.2 tooling remains the accepted content-production gate for P5.4–P5.6 lesson batches.

## P5.4 — Curriculum Expansion Set C — ACTIVE / AUTOMATED QA1 PASS

Issue #80, draft PR #81, branch `phase5/p5-4-curriculum-set-c`.

Set C adds five production lessons:
1. Happy Lines
2. Shape Friends
3. Rainbow Weather
4. Tree Through Seasons
5. Ice Cream Shop

Production catalog is now **14 release lessons**.

Implemented under the frozen contract:
- production `LessonPackageLoader` / `LessonCatalog` only;
- no lesson-ID-specific product/runtime branches;
- Happy Lines + Shape Friends provide real foundational line/shape progression with selected Trace and open authorship turns;
- Rainbow Weather provides exactly three large prepared coloring regions and open weather details;
- Tree Through Seasons supports Draw With Me + Watch Then Draw, has no Trace Help, and ends with open seasonal/story authorship;
- Ice Cream Shop builds cone/base → scoops → sign → open topping/sign customization;
- Companion V2 behavior remains generic/read-only across all five lessons and four supported profile bands;
- one generic Trace/open-authorship compatibility clarification is documented by ADR-008 rather than lesson-specific code.

### QA1 automated evidence

- versionName: `0.5.0-curriculum-expansion-p5.4-qa1`;
- versionCode: 23;
- exact QA commit: `797d2219c4fe7f643d31f1ada42e08bacf7d105f`;
- complete Set-C CI #508 / run `34830695250` — GREEN;
- exact frozen QA1 CI #509 / run `34831113980` — GREEN;
- profile artifact: `10342178179`;
- profile APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.4_QA1-profile.apk`;
- profile APK size: `16,267,569 bytes`;
- profile APK SHA-256: `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`;
- debug artifact: `10341973952`;
- debug APK size: `20,480,506 bytes`;
- debug APK SHA-256: `c47aa4167716ceb9123b683f23c546fb6460d5160ac80e060188db4038360e9d`;
- content-quality artifact: `10342606597`;
- content-quality report: **14 lessons / 0 errors / 3 reviewed warnings**;
- warning code is only `NO_JOURNEY_MEMBERSHIP`, exactly for `rainbow-weather`, `tree-through-seasons`, `ice-cream-shop`;
- any other warning remains a CI failure;
- permission allowlist passed for debug + profile APKs.

Authoritative QA record: `docs/10-execution/P5_4_QA.md`.

### P5.4 gates still pending

Do not claim P5.4 complete or merge PR #81 until these are genuinely performed:
1. interactive Content Lab visual inspection for all five Set-C lessons;
2. focused physical-device pass using exact profile artifact `10342178179`;
3. record device/API and actual pass/fail results;
4. if defects change code/content, cut a new QA candidate/versionCode and repeat evidence;
5. final acceptance-doc exact-head CI;
6. PR #81 ready + squash merge;
7. merged-main Android CI green;
8. close issue #80 completed.

## Frozen architecture invariants

- UI never owns artwork/history/lesson truth.
- Lessons remain structured content; no lesson-ID-specific tutorial/companion screens.
- `LessonSessionState` remains the only teaching-state truth.
- Companion presentation cannot mutate Help level, completion, artwork or persistence.
- Teacher/trace/help/reference overlays never become child artwork.
- AndroidX Ink stays behind owned drawing infrastructure boundaries.
- Coloring/fill stays below protected line art.
- Free Draw remains lesson-independent with explicit provenance.
- Core remains offline-first with no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions.
- P5.7 owns future local adaptive teaching policy.

## Immediate next action

1. Commit the P5.4 QA evidence + continuation-doc synchronization and require exact-head CI green.
2. Inspect all five new lessons in engineering-only Content Lab and record the real result.
3. Install exact profile artifact `10342178179` and run the focused P5.4 physical matrix.
4. Only after genuine acceptance, finish P5.4 docs/merge gates.

## Continuation rule

Read in order:
1. `PROJECT_STATUS.md`
2. `docs/HANDOFF.md`
3. `ROADMAP.md`
4. epic #73
5. issue #80 / PR #81
6. `docs/10-execution/P5_4_EXECUTION_CONTRACT.md`
7. `docs/10-execution/P5_4_QA.md`
8. `docs/10-execution/P5_4_CONTENT_QA.md`
9. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
10. P5.2 QA record only when content-tooling evidence is needed.
