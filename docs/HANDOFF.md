# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Current state

- Phase 0–4 — COMPLETE/frozen.
- Phase 5 / `0.5.0-curriculum-expansion` — ACTIVE.
- Parent epic: #73.
- P5.1–P5.5 — COMPLETE.
- P5.5 verified main baseline: `6412e0e6cf346837b26e925cebc89662c27fba2c`, merged-main CI #528 GREEN.
- Current slice: **P5.6 Curriculum Expansion Set E #84**.
- Branch: `phase5/p5-6-curriculum-set-e`.
- Draft PR: #85.
- Current head: `9244e776707826e613a0bec43a22f60edaae3ed0`.
- P5.6 implementation: **COMPLETE at 24 lessons**.
- Automated final content gate: **GREEN — CI #534**.
- Pre-freeze Content Lab: **PENDING 16/16**.
- P5.6 versionCode 25: **NOT CUT YET**.

## P5.6 locked Set E

1. Face & Expressions — Growing + Young, D3.
2. Simple Body & Pose — Growing + Young, D4.
3. Create Your Character — Growing + Young, D4.
4. One-Point Room — Young, D5.

Stable IDs:
- `face-and-expressions@1`
- `simple-body-and-pose@1`
- `create-your-character@1`
- `one-point-room@1`

Character Creator:
Face & Expressions → Simple Body & Pose → Create Your Character.

Prerequisites:
- Face: none
- Body: Face
- Character: Body
- One-Point Room: Sailboat Scene

One-Point Room remains intentionally standalone; do not invent a journey for it.

## Implementation checkpoints

- execution contract first commit `f11f2a6dc857369651ee8a5cb33d95532736bd2b`; contract/docs CI #529 GREEN.
- Batch A Face + Body commit `fd44ab754e84191d3934f373c1dac05bcca51d1b`; 22-lesson CI #530 GREEN.
- Batch B Character + Room commit `0393274a8d17290c88a49e9a383d155030fefcae`.
- CI #533 found one stale historical analyzer expectation (`lessonTargetMet` still expected false).
- test-only correction `9244e776707826e613a0bec43a22f60edaae3ed0` now requires all final Phase-5 coverage targets.
- exact-head CI #534 / run `34843509513` GREEN.

## Final verified 24-lesson report

- lessons **24**
- errors **0**
- warnings **6**, all `NO_JOURNEY_MEMBERSHIP`
- warning IDs only: Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene, One-Point Room
- age coverage Little **8**, Creative **18**, Growing **17**, Young **10**
- difficulty D1 **5**, D2 **9**, D3 **6**, D4 **3**, D5 **1**
- DWM **23**, WTD **17**, Trace **4**
- coloring **4**, prepared coloring **3**
- all Phase-5 target flags met

Set-E behavior remains:
- no Trace mode/help;
- mature construction/anchor Help;
- open final authorship turns have no expected child strokes;
- One-Point Room vanishing point is guide-only, never a required tiny mark;
- Companion integration remains generic/non-scoring.

## Pre-freeze inspection binary

Engineering-only CI #534 profile artifact:
- artifact ID `10346199935`
- exact head `9244e776707826e613a0bec43a22f60edaae3ed0`
- APK size **16,311,900 bytes**
- SHA-256 `11d9459cbeefff3ff26a457b7a28b7e082ce08b55c8c8b70ce546444e24b4867`
- content-quality artifact `10347106293`

The artifact name/version still carries the P5.5 QA1 label because P5.6 versionCode 25 and workflow artifact rename are intentionally deferred until Content Lab acceptance. Treat this binary only as an engineering inspection build.

## Immediate continuation

1. Read `docs/10-execution/P5_6_PRE_FREEZE_CONTENT_LAB_CHECKLIST.md`.
2. Launch engineering-only Content Lab and inspect all four Set-E lessons.
3. Require **16/16 PASS** with no content-changing defect.
4. Only then freeze `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode 25 and update CI artifact names.
5. Run exact v25 QA1 CI; record immutable profile/debug artifact IDs, APK sizes and SHA-256.
6. Run focused physical acceptance on exact v25 profile binary.
7. Commit genuine acceptance → exact-head CI → PR #85 ready → squash merge → merged-main CI → close #84.
8. Start P5.7 only from verified post-P5.6 main.

## Frozen constraints

- generic structured lessons only;
- no lesson-ID-specific runtime UI;
- `LessonSessionState` is teaching-state truth;
- Companion cannot mutate session/artwork/persistence;
- teacher/help/reference overlays never enter child artwork;
- AndroidX Ink remains behind owned drawing infrastructure;
- offline/account-free/ad-free core;
- no similarity scoring, grades, rank, stars/XP, permanent ability labels, punitive streaks or cloud child profiling.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #84 / PR #85
5. `docs/10-execution/P5_6_EXECUTION_CONTRACT.md`
6. `docs/10-execution/P5_6_PRE_FREEZE_CONTENT_LAB_CHECKLIST.md`
7. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
8. P5.2 tooling docs as needed.
