# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Current state

- Phase 0–4 — COMPLETE/frozen.
- Phase 5 / `0.5.0-curriculum-expansion` — ACTIVE.
- Parent epic: #73.
- P5.1–P5.6 — COMPLETE/frozen.
- Verified P5.6 merged main: `91bc7584994224852d3d44749e78749d39ff954b`.
- P5.6 merged-main CI #552 / run `34849748403`: **GREEN**.
- Current slice: **P5.7 Local Adaptive Teaching #86**.
- Branch: `phase5/p5-7-local-adaptive-teaching`.
- P5.7 execution contract first commit: `5225e82bcae4afae7a0d5dc65bacd1a5054d4b19`.
- Adaptive implementation: **NOT STARTED**.
- Expected first P5.7 QA: `0.5.0-curriculum-expansion-p5.7-qa1`, versionCode **26** — not cut yet.

## Frozen P5.6 baseline

24 production lessons remain the accepted curriculum baseline:
- 0 quality errors;
- 6 reviewed standalone warnings;
- Little 8 / Creative 18 / Growing 17 / Young 10;
- D1 5 / D2 9 / D3 6 / D4 3 / D5 1;
- DWM 23 / WTD 17 / Trace 4.

P5.6 accepted QA1:
- versionCode 25;
- Content Lab 16/16 PASS;
- physical 36/36 PASS;
- profile artifact `10348300909`;
- profile SHA-256 `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`;
- device/API was not provided and was not inferred.

## Existing architecture P5.7 must extend

Home already owns deterministic recommendation/projection behavior:
- `StudioHomeRepository` projects catalog + persisted lesson/coloring state.
- `StudioRecommendationPolicy` currently ranks age fit → explicit interest → preferred mode → age-target difficulty → stable lesson identity.
- `StudioPrimarySelectionPolicy` guarantees active coloring resume → drawing resume → fresh recommendation.

Profile/session truth already exists:
- `ChildProfile`: explicit age band, interests, teaching mode, pace, handedness, narration preference.
- `LessonSessionState`: only teaching-state truth.
- `LessonSessionSnapshot`: lesson/mode/pace/step/help/recovery state.

P5.7 must not create competing UI truth, a cloud intelligence system, analytics, or an artwork evaluator.

## P5.7 owned contract

Read `docs/10-execution/P5_7_EXECUTION_CONTRACT.md` before implementation.

The new owned `product/adaptive` layer is advisory and local only. It may contain bounded versioned state, deterministic reducers/projections, explainable fresh-recommendation policy inputs/reasons, and child-controlled Help suggestion policy.

Allowed signals:
- explicit age/interests/mode/pace;
- authored lesson metadata;
- genuine completed lessons and first-completion skill exposure;
- bounded recent completion history;
- child-requested Help summaries;
- current resumable work only for preserving precedence.

Forbidden:
- strokes/artwork/quality/talent inference;
- permanent ability labels;
- score/grade/rank/XP/punitive streaks;
- cloud profiling/analytics upload;
- network requirement;
- automatic Help escalation;
- hidden demotion;
- lesson-ID-specific adaptive branches.

## P5.7 delivery gates

1. contract/docs exact-head CI GREEN;
2. Batch A — adaptive state model/store/reducer + idempotence/privacy/corruption tests → CI GREEN;
3. Batch B — progression-aware fresh recommendations + explainable reasons, preserving resume precedence → CI GREEN;
4. Batch C — child-controlled adaptive Help over existing authored Help options → CI GREEN;
5. focused deterministic/offline/corruption/privacy QA;
6. freeze QA1 only then at versionCode 26;
7. exact APK evidence + physical acceptance;
8. acceptance CI → squash merge → merged-main CI → close #86.

## Immediate continuation

- Open the draft P5.7 PR after continuation docs are synchronized.
- Require exact-head contract/docs CI GREEN before adding adaptive implementation.
- Do **not** spend versionCode 26 during contract or intermediate batches.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #86 / P5.7 draft PR
5. `docs/10-execution/P5_7_EXECUTION_CONTRACT.md`
6. existing `StudioHomeModels.kt`, `StudioHomeRepository.kt`, `StudioPrimarySelectionPolicy.kt`, `ChildProfile.kt`, `LessonSessionModels.kt` as implementation context.
