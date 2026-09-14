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
- P5.2 #76 — COMPLETE; PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`; merged-main CI #482 GREEN
- Current slice: **P5.3 Companion / Teacher Experience V2 #78**
- Draft PR: #79
- Active branch: `phase5/p5-3-companion-teacher-v2`
- Latest fully verified product release remains **`0.4.0-content-studio`, versionCode 19**
- P5.3 QA1 `0.5.0-curriculum-expansion-p5.3-qa1`, versionCode 22 — **focused physical/product acceptance PASS; final docs/merge gates pending**

## Exact accepted 0.4 baseline

- physically tested executable `f3843365d39540de00fe08a008883c15abe75599`
- final squash merge `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`
- final closure `main` head `e8215bedea6f8d55c0ca08ca4015a46062d45769`
- accepted candidate CI #440 GREEN
- merged-main CI #446 GREEN
- closure-doc CI #450 GREEN
- profile artifact `10331363240`
- APK size `16,196,353 bytes`
- SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`
- final physical/product checklist PASS

## P5.1 — COMPLETE curriculum contract

Locked target:
- 24 total production guided lessons for 0.5;
- 9 existing Phase-4 lessons retained;
- 15 new lessons;
- Little/Creative/Growing/Young progression is real rather than cosmetic;
- creative authorship increases with age/difficulty;
- no similarity scores, grades, leaderboards, permanent ability labels or cloud profiling;
- Help remains child-controlled/non-punitive.

## P5.2 — COMPLETE content-production system

Accepted QA2:
- `0.5.0-curriculum-expansion-p5.2-qa2`, versionCode 21;
- executable `2f36834d110cb1076b953f519eee4a8dc6e2e19d`;
- exact-head CI #477 GREEN;
- profile artifact `10333593024`;
- APK size `16,245,548 bytes`;
- SHA-256 `725720ae1fbff232cbb56049d77b087d8214f959189269630dbbd9eeb0817cf6`;
- report 9 lessons / 0 errors / 0 warnings;
- Content Lab focused matrix 20/20 PASS;
- final acceptance-doc CI #481 GREEN;
- PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`;
- merged-main CI #482 GREEN;
- issue #76 closed completed.

Content Lab remains engineering-only; `ProductActivity` remains the normal launcher.

## P5.3 — PHYSICALLY ACCEPTED / MERGE GATES PENDING

Authoritative files:
- `docs/10-execution/P5_3_EXECUTION_CONTRACT.md`
- `docs/10-execution/P5_3_QA.md`

### Architecture

- `LessonSessionState` + validated lesson package remain the only teaching truth.
- `ProductLessonPresentationPolicy` is pure/read-only presentation derivation.
- Product profile `AgeBand` controls tone only; content-model age eligibility remains separate.
- Companion never owns progress, Help level, completion, artwork or persistence.
- No lesson-ID-specific companion branches.
- No scoring, similarity judgement, ranks, grades, stars, XP, streaks or permanent ability labels.
- P5.7 remains owner of future local adaptive teaching.

### Implemented Companion V2

- age-specific deterministic wording for Little / Creative / Growing / Young;
- clear teacher demo vs child turn vs Help vs pause vs completion semantic presentation;
- Watch Then Draw whole-overview copy remains distinct from per-step demo;
- generic open-choice detection via `MANUAL_DONE + expectedStrokeRefs.isEmpty()`;
- open-choice copy supports authorship without copy/match pressure;
- Trace/strong Help described as practice support, never scoring/failure;
- deterministic secondary cues for Replay/Help/Watch/Trace/context;
- optional age-specific reflection at completion;
- completion celebrates authorship/process without grading;
- Guided Lesson passes the existing child profile age band into policy;
- secondary cue renders inside companion card;
- reflection appears only at post-drawing boundary and never blocks color/finish actions;
- legacy hardcoded `Cute Cat` / `save your cat` fallback copy removed;
- all existing lesson commands/control visibility preserved.

### Accepted QA1 evidence

- versionName `0.5.0-curriculum-expansion-p5.3-qa1`;
- versionCode 22;
- physically tested executable `eef87b25478c6a30d6fefbd7580f75ded4eca3ab`;
- corrected pure-policy CI #486 / run `34810895376` GREEN;
- integrated Guided Lesson CI #487 / run `34811189264` GREEN;
- exact frozen QA1 CI #493 / run `34811688427` GREEN;
- debug artifact `10335460247`;
- profile artifact `10334873551`;
- content-quality artifact `10334589314`;
- profile APK size `16,245,553 bytes`;
- SHA-256 `bed4c00bce4629822b50c6523527a1ebad66428fc0977580501a20c77ad3e5de`;
- independent local SHA/size recomputation MATCHED CI evidence;
- content-quality report 9 lessons / 0 errors / 0 warnings;
- focused physical/product matrix: **20/20 PASS** by user report on 2026-09-14;
- device model/API were not restated and are not inferred.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons remain structured content, never lesson-ID-specific tutorial code;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help/reference overlays never become child artwork;
- coloring/fill remains structurally below protected line art;
- Free Draw remains lesson-independent with explicit Gallery provenance;
- persistence stores editable operations, not screenshots;
- core product remains offline-first;
- no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions;
- engine changes require a concrete defect + explicit contract/ADR.

## Immediate continuation

1. Run final exact-head acceptance-doc CI on the evidence-synchronized P5.3 branch.
2. If green, mark PR #79 ready and squash-merge with its exact head SHA.
3. Verify merged-main Android CI on the squash merge.
4. Close issue #78 completed only after merged-main green.
5. Begin P5.4 only from verified P5.3 `main`.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. epic #73
5. issue #78 / PR #79
6. `docs/10-execution/P5_3_EXECUTION_CONTRACT.md`
7. `docs/10-execution/P5_3_QA.md`
8. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
9. P5.2 QA record only when content-tooling evidence is needed.

Do not reopen proven foundations merely because a chat changes.
