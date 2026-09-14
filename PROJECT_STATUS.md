# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product milestone:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE**  
**Active branch:** `phase5/p5-3-companion-teacher-v2`  
**Parent epic:** #73  
**Current slice:** P5.3 — Companion / Teacher Experience V2 #78  
**Draft PR:** #79  
**Last updated:** 2026-09-14

Git is authoritative when chat memory and repository state disagree.

## Completed milestones

- Phase 0 — Product Foundation — COMPLETE.
- Phase 1 — Drawing Engine `0.1.0-art-lab`, versionCode 11 — COMPLETE/frozen.
- Phase 2 — Lesson Engine `0.2.0-lesson-engine`, versionCode 12 — COMPLETE/frozen; physical 32/32 PASS.
- Phase 3 — First Vertical Slice `0.3.0-vertical-slice`, versionCode 13 — COMPLETE; physical 41/41 PASS.
- Phase 4 — Content & Studio Expansion `0.4.0-content-studio`, versionCode 19 — COMPLETE/frozen.
- P5.1 — Curriculum & Teaching Contract #74 — COMPLETE; PR #75 squash-merged at `cea06e219290c82b9a1f8f8007069c61841bbc95`; merged-main CI #452 GREEN.
- P5.2 — Content Production System V2 #76 — COMPLETE; PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`; merged-main CI #482 / run `34809175064` GREEN.

No 0.2/0.3/0.4 tag is claimed unless separately verified. Phase 4 remains frozen.

## Exact accepted 0.4 baseline

- physically tested executable `f3843365d39540de00fe08a008883c15abe75599`;
- final Phase-4 squash merge `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`;
- final closure `main` head `e8215bedea6f8d55c0ca08ca4015a46062d45769`;
- accepted candidate CI #440 GREEN;
- merged-main CI #446 GREEN;
- closure-doc CI #450 GREEN;
- profile artifact `10331363240`;
- profile APK size `16,196,353 bytes`;
- SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`;
- final physical/product checklist PASS.

## Phase 5 target locked by P5.1

Target milestone: `0.5.0-curriculum-expansion`.

- 24 total production guided lessons;
- 9 verified Phase-4 lessons retained + 15 purposeful new lessons;
- all four age bands receive real progression;
- guidance decreases and creative authorship increases with age;
- no similarity scoring, permanent ability labels, punitive streaks or cloud profiling;
- structured/generic offline-first content;
- P5.2 tooling is the accepted content-production gate for later P5.4–P5.6 lesson batches.

## P5.2 — COMPLETE

Accepted QA2 evidence:
- `0.5.0-curriculum-expansion-p5.2-qa2`, versionCode 21;
- executable `2f36834d110cb1076b953f519eee4a8dc6e2e19d`;
- exact-head CI #477 / run `34808016949` GREEN;
- profile artifact `10333593024`;
- profile APK size `16,245,548 bytes`;
- SHA-256 `725720ae1fbff232cbb56049d77b087d8214f959189269630dbbd9eeb0817cf6`;
- report 9 lessons / 0 errors / 0 warnings;
- focused Content Lab matrix 20/20 PASS;
- final acceptance-doc CI #481 GREEN;
- PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`;
- merged-main CI #482 GREEN;
- issue #76 closed completed.

## P5.3 — Companion / Teacher Experience V2 — PHYSICALLY ACCEPTED / MERGE GATES PENDING

Issue #78, draft PR #79, branch `phase5/p5-3-companion-teacher-v2`.

Implemented:
- pure deterministic `ProductLessonPresentationPolicy` remains read-only over authoritative session/content state;
- existing child profile age band controls age-appropriate teacher tone;
- Little / Creative / Growing / Young wording differs without changing lesson semantics;
- teacher demo, child turn, Help, pause, completion and error states remain distinct;
- Watch Then Draw overview remains distinct from per-step demonstration;
- open-ended authorship is detected generically by `MANUAL_DONE + expectedStrokeRefs.isEmpty()`;
- Trace/Help language normalizes direct support as practice with no scoring/failure pressure;
- optional age-specific reflection appears only after drawing completion and does not block Color/Finish;
- legacy `Cute Cat` / `save your cat` generic fallback wording removed;
- existing session commands/control visibility preserved.

Accepted QA1 evidence:
- `0.5.0-curriculum-expansion-p5.3-qa1`, versionCode 22;
- exact physically tested executable `eef87b25478c6a30d6fefbd7580f75ded4eca3ab`;
- corrected pure-policy CI #486 / run `34810895376` GREEN;
- integrated Guided Lesson CI #487 / run `34811189264` GREEN;
- exact frozen QA1 CI #493 / run `34811688427` GREEN;
- debug artifact `10335460247`;
- profile artifact `10334873551`;
- content-quality artifact `10334589314`;
- profile APK size `16,245,553 bytes`;
- profile APK SHA-256 `bed4c00bce4629822b50c6523527a1ebad66428fc0977580501a20c77ad3e5de`;
- independent SHA/size recomputation MATCHED CI evidence;
- content quality remains 9 lessons / 0 errors / 0 warnings;
- focused physical/product matrix 20/20 PASS by user report on 2026-09-14;
- device model / Android API were not restated by the user and are intentionally not inferred.

Authoritative QA record: `docs/10-execution/P5_3_QA.md`.

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
- P5.7, not P5.3, owns future local adaptive teaching policy.

## Immediate next action

1. Run final exact-head acceptance-doc CI after this evidence synchronization.
2. If green, mark PR #79 ready and squash-merge with expected head SHA.
3. Verify merged-main Android CI on the exact squash merge.
4. Close issue #78 completed only after merged-main green.
5. Then create P5.4 from verified `main` and begin Curriculum Expansion Set C under the frozen Phase-5 contract.

## Continuation rule

Read in order:
1. `PROJECT_STATUS.md`
2. `docs/HANDOFF.md`
3. `ROADMAP.md`
4. epic #73
5. issue #78 / PR #79
6. `docs/10-execution/P5_3_EXECUTION_CONTRACT.md`
7. `docs/10-execution/P5_3_QA.md`
8. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
9. P5.2 QA record only when tooling evidence is needed.
