# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Low-latency native drawing, editable operation history, tools, Undo/Redo/Clear, atomic persistence/recovery and deterministic teacher playback proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lesson loading/validation, all three teaching modes, Help Ladder, five paces, lifecycle/process recovery and 32/32 physical QA proven.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
`0.3.0-vertical-slice`, versionCode 13. Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — COMPLETE
Released milestone: `0.4.0-content-studio` / versionCode 19. Phase 4 proved the reusable offline product foundation and remains the latest fully released milestone while Phase 5 proceeds slice-by-slice.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — ACTIVE

**Target:** `0.5.0-curriculum-expansion`  
**Parent epic:** #73  
**Locked catalog target:** 24 production guided lessons

### P5.1 — Curriculum & Teaching Contract — COMPLETE
Issue #74 / PR #75 complete. Exact age 4–12 progression, content allocation, quality rubric and authorship philosophy locked.

### P5.2 — Content Production System V2 — COMPLETE
Issue #76 / PR #77 complete. Validator, Content Lab, geometry/help/coloring checks and release-quality reporting proven.

### P5.3 — Companion / Teacher Experience V2 — COMPLETE
Issue #78 / PR #79 complete. Deterministic age-aware Companion V2 and generic open-authorship presentation proven; physical 20/20 PASS.

### P5.4 — Curriculum Expansion Set C — COMPLETE
Issue #80 / PR #81 complete; merged-main CI #514 GREEN. Added Happy Lines, Shape Friends, Rainbow Weather, Tree Through Seasons and Ice Cream Shop. Catalog grew 9 → **14**. Content Lab 15/15 and physical 30/30 PASS.

### P5.5 — Curriculum Expansion Set D — ACCEPTED / FINAL CLOSURE IN PROGRESS

Issue #82 / PR #83.

Delivered:
- Snail Garden
- Elephant From Shapes
- Simple Car
- Sailboat Scene
- Planet With Rings
- Friendly Alien

Catalog grew **14 → 20**.

Journey progressions:
- Animal Artist: Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait.
- Space Artist: Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.

Coverage after Set D:
- Little 8
- Creative 18
- Growing 14
- Young 6

Accepted QA1:
- `0.5.0-curriculum-expansion-p5.5-qa1`, versionCode **24**;
- exact QA commit `3a538b5f7c2db118a0006176b7093b0e22961f9b`;
- Android CI #525 GREEN;
- evidence/checklist CI #526 GREEN;
- quality **20 lessons / 0 errors / exactly 5 reviewed standalone warnings**;
- profile artifact `10344519403`;
- profile APK **16,293,898 bytes**;
- profile SHA-256 `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`;
- Content Lab **18/18 PASS**;
- exact-profile physical **36/36 PASS**;
- device/API not provided and not inferred;
- no binary/content-changing defect reported.

Remaining closure: final acceptance-doc CI → PR ready → squash merge → merged-main CI → close #82.

### P5.6 — Curriculum Expansion Set E — NEXT

People/characters + older-child technique:
- Face & Expressions
- Simple Body & Pose
- Create Your Character
- One-Point Room

Target: **20 → 24 lessons**.

P5.6 must deliberately complete the older-child/Young Artist technique floor with credible expression, proportion, pose, character construction and one-point perspective. Guidance should decrease as age/difficulty rises; creative authorship should increase. P5.6 starts only from verified post-P5.5 `main`, with its execution contract committed before content implementation.

### P5.7 — Local Adaptive Teaching

Deterministic/offline suggestions may use age, interests, completed skills, resume state, child-requested Help patterns and recent difficulty mix.

Allowed: suggest guided practice, alternate subject practice, slightly greater independence/challenge, or creative/free-draw extension.

Not allowed: permanent ability labels, cloud profiling, behavioral analytics upload, grades/rank/streak punishment, forced demotion or ML talent/quality judgement.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Must verify all 24 release lessons, all four age bands, journeys, Companion, local adaptive teaching, offline/lifecycle/Gallery/recovery regressions, content-production report, exact final APK evidence and merged-main CI.

## Later direction after 0.5

Potential later milestones: Parent Zone, accessibility/device hardening, culturally specific/folk-art curriculum after sourcing/review policy is operational, public Beta/store readiness, expansion from 24 toward the 36-lesson public-V1 target, then V1.0 after product/privacy/safety/content/store gates pass.

## Permanent delivery rule

Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
