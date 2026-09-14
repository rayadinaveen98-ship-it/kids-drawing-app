# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Current state

- Phase 0–4 — COMPLETE/frozen.
- Phase 5 / `0.5.0-curriculum-expansion` — ACTIVE.
- Parent epic: #73.
- P5.1–P5.5 — COMPLETE.
- Current slice: **P5.6 Curriculum Expansion Set E #84**.
- Branch: `phase5/p5-6-curriculum-set-e`.
- Draft PR: #85.
- Catalog: **24 production lessons**.
- Pre-freeze Content Lab: **16/16 PASS**.
- QA1: `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode **25**.
- Exact QA1 binary head: `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`.
- QA1 Android CI #538 / run `34846235868`: **GREEN**.
- Physical acceptance: **PENDING 36/36**.

## P5.6 delivered Set E

1. Face & Expressions — Growing + Young, D3.
2. Simple Body & Pose — Growing + Young, D4.
3. Create Your Character — Growing + Young, D4.
4. One-Point Room — Young, D5.

Character Creator:
Face & Expressions → Simple Body & Pose → Create Your Character.

Prerequisites:
- Face: none
- Body: Face
- Character: Body
- One-Point Room: Sailboat Scene

One-Point Room remains intentionally standalone.

## Verified implementation checkpoints

- contract first commit `f11f2a6dc857369651ee8a5cb33d95532736bd2b`; CI #529 GREEN.
- Batch A `fd44ab754e84191d3934f373c1dac05bcca51d1b`; 22-lesson CI #530 GREEN.
- Batch B `0393274a8d17290c88a49e9a383d155030fefcae`.
- final analyzer expectation correction `9244e776707826e613a0bec43a22f60edaae3ed0`; 24-lesson CI #534 GREEN.
- pre-freeze docs/checklist `c3ea5d6cfe9394382fdc502ed43ec440d6189362`; CI #535 GREEN.
- Content Lab 16/16 PASS recorded at `7418dd59492dfd9a6a153083385dfb113ac89adf`.
- versionCode 25 cut and P5.6 artifact packaging active by head `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`.
- QA1 CI #538 GREEN.

## Final verified content report

- lessons **24**
- errors **0**
- warnings **6**, all reviewed `NO_JOURNEY_MEMBERSHIP`
- warning lessons only: Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene, One-Point Room
- age counts Little **8**, Creative **18**, Growing **17**, Young **10**
- difficulty D1 **5**, D2 **9**, D3 **6**, D4 **3**, D5 **1**
- DWM **23**, WTD **17**, Trace **4**
- all Phase-5 coverage targets met

Set E retains no Trace mode/help, mature construction/anchor Help, open-authorship final turns with empty expected refs, and generic non-scoring Companion behavior. One-Point Room's vanishing point is guide-only.

## Immutable P5.6 QA1 evidence

### Profile — use this for physical acceptance
- artifact `10348300909`
- `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-profile.apk`
- size **16,311,900 bytes**
- SHA-256 `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`

### Debug
- artifact `10347578783`
- size **20,527,518 bytes**
- SHA-256 `5e8eb2b9ff209798bdbb7d568326d4d4df3aff973821de2e14a333f2e12091e8`

### Content quality
- artifact `10347409267`

## Immediate continuation

1. Use only the exact profile APK above.
2. Execute `docs/10-execution/P5_6_FOCUSED_ACCEPTANCE_CHECKLIST.md`.
3. Require genuine **36/36 PASS**. Do not infer physical acceptance from CI or Content Lab.
4. If PASS: record acceptance, run exact-head acceptance-doc CI, mark PR #85 ready, squash merge, verify merged-main CI, close #84.
5. If a binary/content-changing defect appears: keep PR draft and cut a new candidate with versionCode >25 after fixing/retesting.
6. Start P5.7 only from verified post-P5.6 main.

Device model/API must not be invented; record only if actually supplied by the tester.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #84 / PR #85
5. `docs/10-execution/P5_6_EXECUTION_CONTRACT.md`
6. `docs/10-execution/P5_6_QA.md`
7. `docs/10-execution/P5_6_FOCUSED_ACCEPTANCE_CHECKLIST.md`
8. `docs/10-execution/P5_6_PRE_FREEZE_CONTENT_LAB_CHECKLIST.md`

## Frozen constraints

- generic structured lessons only;
- no lesson-ID-specific runtime UI;
- `LessonSessionState` is teaching-state truth;
- Companion cannot mutate session/artwork/persistence;
- teacher/help/reference overlays never enter child artwork;
- AndroidX Ink remains behind owned drawing infrastructure;
- offline/account-free/ad-free core;
- no similarity scoring, grades, rank, stars/XP, permanent ability labels, punitive streaks or cloud child profiling.
