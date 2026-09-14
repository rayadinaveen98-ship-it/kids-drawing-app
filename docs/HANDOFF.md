# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Current state

- Phase 0–4 — COMPLETE/frozen.
- Phase 5 / `0.5.0-curriculum-expansion` — ACTIVE.
- Parent epic: #73.
- P5.1 #74 — COMPLETE.
- P5.2 #76 — COMPLETE.
- P5.3 #78 — COMPLETE; merged-main CI #498 GREEN; physical 20/20 PASS.
- P5.4 #80 — COMPLETE; merged-main CI #514 GREEN; Content Lab 15/15 + physical 30/30 PASS.
- P5.5 #82 — COMPLETE; PR #83 squash-merged at `6412e0e6cf346837b26e925cebc89662c27fba2c`; merged-main CI #528 GREEN; Content Lab 18/18 + physical 36/36 PASS.
- Current slice: **P5.6 Curriculum Expansion Set E #84**.
- Active branch: `phase5/p5-6-curriculum-set-e`.
- P5.6 content implementation: **NOT STARTED**.
- Current catalog: **20 lessons**; P5.6 target: **24**.

## Verified starting baseline

P5.6 starts only from verified main:
`6412e0e6cf346837b26e925cebc89662c27fba2c`

Merged-main Android CI #528 / run `34840361689` is GREEN.

P5.5 accepted profile evidence remains:
- versionCode 24;
- artifact `10344519403`;
- APK 16,293,898 bytes;
- SHA-256 `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`.

## P5.6 locked contract

Execution contract: `docs/10-execution/P5_6_EXECUTION_CONTRACT.md`  
First P5.6 branch commit: `f11f2a6dc857369651ee8a5cb33d95532736bd2b`  
This commit contains the contract before any Set-E lesson assets.

Locked lessons:
1. **Face & Expressions** — Growing + Young; D3; face construction, landmarks, expression variation; DWM + WTD; Character Creator entry.
2. **Simple Body & Pose** — Growing + Young; D4; body construction, proportion, pose, silhouette; DWM + WTD; Character Creator progression.
3. **Create Your Character** — Growing + Young; D4; synthesized authored character design; no similarity requirement.
4. **One-Point Room** — Young only; D5; one-point perspective/depth scale/scene composition; standalone advanced technique.

Stable identities:
- `face-and-expressions@1`
- `simple-body-and-pose@1`
- `create-your-character@1`
- `one-point-room@1`

Character Creator journey:
Face & Expressions → Simple Body & Pose → Create Your Character.

Prerequisites:
- Face: none
- Body: Face
- Character: Body
- One-Point Room: Sailboat Scene

One-Point Room is intentionally standalone; do not invent a journey merely to remove its reviewed warning.

## Final Phase-5 curriculum checkpoint expected after P5.6

Age coverage:
- Little 8
- Creative 18
- Growing 17
- Young 10

Difficulty:
- D1 5
- D2 9
- D3 6
- D4 3
- D5 1

Quality warning policy after Set E:
exactly six reviewed `NO_JOURNEY_MEMBERSHIP` warnings only for:
- Rainbow Weather
- Tree Through Seasons
- Ice Cream Shop
- Simple Car
- Sailboat Scene
- One-Point Room

No other warning/error accepted.

## Planned P5.6 execution

1. continuation docs + draft PR;
2. contract/docs CI GREEN;
3. Batch A: Face & Expressions + Simple Body & Pose → 22 lessons;
4. exact-head CI GREEN;
5. Batch B: Create Your Character + One-Point Room → 24 lessons;
6. final quality/age/difficulty/Character Creator + Companion gates;
7. Content Lab all four;
8. freeze `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode **25**;
9. immutable APK evidence + exact-profile physical acceptance;
10. acceptance CI → squash merge → merged-main CI → close #84.

## Frozen constraints

- generic structured lessons only;
- no lesson-ID-specific runtime UI;
- `LessonSessionState` is teaching-state truth;
- Companion is read-only relative to session/artwork;
- no Trace for Set-E older-child content;
- teacher/help/reference overlays never enter child artwork;
- AndroidX Ink remains behind owned drawing infrastructure;
- offline/account-free/ad-free core remains unchanged;
- no scoring/rank/stars/XP/permanent labels/punitive streaks/cloud profiling.

## Resume protocol

Read:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #84
5. `docs/10-execution/P5_6_EXECUTION_CONTRACT.md`
6. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
7. P5.2 tooling docs as needed
8. P5.5 QA only for frozen baseline evidence.
