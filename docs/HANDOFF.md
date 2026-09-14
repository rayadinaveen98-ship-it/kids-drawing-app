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
- P5.4 #80 — COMPLETE; PR #81 merged; merged-main CI #514 GREEN; Content Lab 15/15 + physical 30/30 PASS.
- P5.5 #82 — **ACCEPTED; FINAL CLOSURE IN PROGRESS**.
- PR #83; branch `phase5/p5-5-curriculum-set-d`.
- Catalog: **20 release lessons**.
- QA1: `0.5.0-curriculum-expansion-p5.5-qa1`, versionCode **24**.
- Exact QA app/content commit: `3a538b5f7c2db118a0006176b7093b0e22961f9b`.

## P5.5 accepted result

Delivered Set D:
- Snail Garden
- Elephant From Shapes
- Simple Car
- Sailboat Scene
- Planet With Rings
- Friendly Alien

Implemented journeys:
- Animal Artist: Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait.
- Space Artist: Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.

Simple Car and Sailboat Scene remain intentionally standalone.

Coverage checkpoint:
- Little 8
- Creative 18
- Growing 14
- Young 6

Automated QA:
- Android CI #525 GREEN on exact QA binary commit;
- evidence/checklist CI #526 GREEN;
- 20 lessons / 0 errors / exactly 5 reviewed `NO_JOURNEY_MEMBERSHIP` warnings;
- only Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car and Sailboat Scene warn.

Exact profile APK:
- artifact `10344519403`;
- APK size **16,293,898 bytes**;
- SHA-256 `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`.

Interactive acceptance reported 2026-09-14:
- Content Lab **18/18 PASS**;
- exact-profile physical **36/36 PASS**;
- no binary/content-changing defect reported;
- device model/API not provided and not inferred.

Authoritative acceptance docs:
- `docs/10-execution/P5_5_QA.md`
- `docs/10-execution/P5_5_FOCUSED_ACCEPTANCE_CHECKLIST.md`
- `docs/10-execution/P5_5_EXECUTION_CONTRACT.md`

## Immediate continuation

1. require final acceptance-doc exact-head CI GREEN;
2. mark PR #83 ready;
3. squash-merge verified head;
4. verify merged-main Android CI GREEN;
5. close #82 completed;
6. create P5.6 issue/branch/draft PR from that verified `main`;
7. lock P5.6 execution contract before authoring Set-E content.

## P5.6 next scope

Set E is exactly:
1. Face & Expressions
2. Simple Body & Pose
3. Create Your Character
4. One-Point Room

Target catalog: **20 → 24 lessons**. The emphasis is people/characters plus older-child proportion, pose, expression, perspective and creative authorship. Do not dilute younger lessons merely to inflate Young Artist coverage.

## Frozen constraints

- lessons remain generic structured content;
- no lesson-ID-specific product/runtime branches;
- `LessonSessionState` remains teaching-state truth;
- Companion presentation does not mutate Help/completion/artwork/persistence;
- overlays never become child artwork;
- AndroidX Ink remains behind owned drawing infrastructure;
- offline/account-free/ad-free core remains unchanged;
- no scoring/rank/stars/XP/permanent ability labels/punitive streaks/cloud child profiling;
- engine changes require a concrete defect + explicit contract/ADR.
