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

P5.2 delivered deterministic content-quality reporting and the read-only Content Lab without creating a second runtime.

Accepted QA2 evidence:
- `0.5.0-curriculum-expansion-p5.2-qa2`, versionCode 21;
- executable `2f36834d110cb1076b953f519eee4a8dc6e2e19d`;
- exact-head CI #477 / run `34808016949` GREEN;
- profile artifact `10333593024`;
- profile APK size `16,245,548 bytes`;
- SHA-256 `725720ae1fbff232cbb56049d77b087d8214f959189269630dbbd9eeb0817cf6`;
- independent SHA/size verification PASS;
- report 9 lessons / 0 errors / 0 warnings;
- focused Content Lab matrix 20/20 PASS and normal-product smoke PASS;
- final acceptance-doc CI #481 GREEN;
- PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`;
- merged-main CI #482 GREEN;
- issue #76 closed completed.

QA record: `docs/10-execution/P5_2_QA.md`.

## P5.3 — Companion / Teacher Experience V2 — ACTIVE / QA1 FREEZE

Issue #78, draft PR #79, branch `phase5/p5-3-companion-teacher-v2`.

Execution contract: `docs/10-execution/P5_3_EXECUTION_CONTRACT.md`.
QA matrix: `docs/10-execution/P5_3_QA.md`.

Implemented:
- pure deterministic `ProductLessonPresentationPolicy` remains read-only over authoritative session/content state;
- existing child profile age band now controls age-appropriate teacher tone;
- Little / Creative / Growing / Young wording differs without changing lesson semantics;
- teacher demo, child turn, Help, pause, completion and error states remain distinct;
- Watch Then Draw overview remains distinct from per-step demonstration;
- open-ended authorship is detected generically by `MANUAL_DONE + expectedStrokeRefs.isEmpty()`; no lesson-ID branch;
- Trace/Help language normalizes direct support as practice with no scoring/failure pressure;
- secondary companion cues are deterministic and presentation-only;
- optional age-specific reflection appears only after drawing completion and does not block Color/Finish;
- legacy `Cute Cat` / `save your cat` generic fallback wording removed from Guided Lesson UI;
- existing session commands/control visibility preserved.

Automated evidence:
- first policy CI #485 found one wording-test mismatch only;
- corrected pure-policy CI #486 / run `34810895376` GREEN;
- integrated Guided Lesson CI #487 / run `34811189264` GREEN;
- all current session states and all four product age bands covered by deterministic JVM policy tests.

### P5.3 QA1 identity — CURRENT

- versionName `0.5.0-curriculum-expansion-p5.3-qa1`;
- versionCode 22;
- dedicated P5.3 QA1 debug/profile artifact packaging configured;
- exact frozen candidate commit / CI / artifact IDs / APK SHA: PENDING final exact-head green build;
- focused physical/product matrix: PENDING.

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

1. Freeze one exact P5.3 QA1 v22 head after QA/status/handoff/roadmap synchronization.
2. Require exact-head Android CI green including unit/lint/debug/instrumentation/profile compile, content-quality gate, permissions and dedicated P5.3 APK artifacts.
3. Download the exact profile artifact and independently verify size/SHA.
4. Hand the exact v22 profile APK to the user for the focused 20-row P5.3 matrix.
5. Record only checks actually reported.
6. After physical acceptance: acceptance-doc CI → PR #79 ready/squash merge → merged-main CI → close #78.
7. Only then advance to P5.4 curriculum expansion.

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
