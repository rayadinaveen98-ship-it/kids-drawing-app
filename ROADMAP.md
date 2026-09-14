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
Released milestone: `0.4.0-content-studio` / versionCode 19. Parent epic #57 closed. Final PR #72 squash-merged at `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`. Merged-main CI #446 GREEN; closure-doc CI #450 GREEN.

Phase 4 proved the reusable offline product foundation: nine production lessons, multi-lesson Studio/categories/Art Journeys, deterministic recommendations/resume, all three teaching modes, Help Ladder/grouped demos, Free Draw, prepared + legacy coloring, lifecycle/recovery, Gallery safety, Airplane Mode core journeys and cross-age final physical QA.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — ACTIVE

**Target:** `0.5.0-curriculum-expansion`  
**Parent epic:** #73  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19

### Milestone strategy

Phase 5 turns the technically proven 0.4 app into a deeper art-learning product without reopening proven engines by default.

Locked catalog target for 0.5:
- 24 total production guided lessons;
- 9 existing verified lessons;
- 15 new lessons;
- 36 remains the later public-V1 target after the 24-lesson workflow/content system is proven.

### P5.1 — Curriculum & Teaching Contract — COMPLETE
Issue #74 closed completed. PR #75 squash-merged at `cea06e219290c82b9a1f8f8007069c61841bbc95`. Exact-head CI #451 GREEN; merged-main CI #452 GREEN.

Phase-5 exact new lesson target remains:
1. Happy Lines
2. Shape Friends
3. Snail Garden
4. Elephant From Shapes
5. Rainbow Weather
6. Tree Through Seasons
7. Ice Cream Shop
8. Simple Car
9. Sailboat Scene
10. Planet With Rings
11. Friendly Alien
12. Face & Expressions
13. Simple Body & Pose
14. Create Your Character
15. One-Point Room

### P5.2 — Content Production System V2 — COMPLETE
Issue #76 closed completed. PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`; merged-main CI #482 GREEN.

Accepted QA2: versionCode 21, profile artifact `10333593024`, exact executable `2f36834d110cb1076b953f519eee4a8dc6e2e19d`, physical Content Lab 20/20 PASS.

### P5.3 — Companion / Teacher Experience V2 — COMPLETE
Issue #78 closed completed. PR #79 squash-merged at `e3553414c591ae5def3d9016c1a63e9d1a350f39`; merged-main CI #498 GREEN.

Delivered deterministic age-aware Companion V2, clearer turn/help/completion presentation, Watch Then Draw overview distinction, generic open-authorship detection, non-punitive Help/Trace language, optional reflection, and preserved session/control semantics. Focused physical matrix 20/20 PASS.

### P5.4 — Curriculum Expansion Set C — COMPLETE
Issue #80 closed completed. PR #81 squash-merged at `00c618cb9b16444f77e534431b3ca417e85a3e10`; merged-main Android CI #514 GREEN.

Delivered Happy Lines, Shape Friends, Rainbow Weather, Tree Through Seasons and Ice Cream Shop. Catalog grew 9 → **14**. Accepted QA1 versionCode 23; Content Lab 15/15 PASS; focused physical 30/30 PASS.

ADR-008 remains the generic Trace/open-authorship compatibility rule.

### P5.5 — Curriculum Expansion Set D — IMPLEMENTED / QA1 AUTOMATED GREEN / INTERACTIVE ACCEPTANCE PENDING

Issue #82, draft PR #83, branch `phase5/p5-5-curriculum-set-d`.

Delivered:
- Snail Garden;
- Elephant From Shapes;
- Simple Car;
- Sailboat Scene;
- Planet With Rings;
- Friendly Alien.

Catalog grew **14 → 20 release lessons**.

Implemented progressions:
- `journey.animal_artist`: Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait;
- `journey.space_artist`: Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.

Simple Car + Sailboat Scene remain intentionally standalone.

Coverage checkpoint achieved:
- Little Artists: **8**;
- Creative Explorers: **18**;
- Growing Artists: **14**;
- Young Artists: **6**.

#### QA1 frozen binary

- versionName `0.5.0-curriculum-expansion-p5.5-qa1`;
- versionCode **24**;
- exact app/content commit `3a538b5f7c2db118a0006176b7093b0e22961f9b`;
- Android CI #525 / run `34837734724` — **GREEN**;
- content quality: **20 lessons / 0 errors / exactly 5 reviewed standalone warnings**;
- profile artifact `10344519403`;
- profile APK **16,293,898 bytes**;
- profile SHA-256 `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`;
- debug artifact `10345315128`;
- content-quality artifact `10344354743`;
- permission allowlist PASS.

Remaining P5.5 gates:
1. QA evidence/checklist exact-head CI green;
2. Content Lab **18/18 PASS**;
3. exact-profile physical **36/36 PASS**;
4. commit acceptance results;
5. final acceptance-doc CI green;
6. mark PR #83 ready;
7. squash-merge;
8. merged-main CI green;
9. close #82 and freeze P5.5.

Do not merge P5.5 based on automated CI alone.

### P5.6 — Curriculum Expansion Set E

People/characters + older-child technique:
- Face & Expressions
- Simple Body & Pose
- Create Your Character
- One-Point Room

Expected catalog growth: **20 → 24 lessons**. P5.6 deliberately completes the remaining Young Artist technique/coverage floor rather than broadening easier lessons to all ages.

P5.6 must start only after P5.5 is squash-merged and merged-main CI is verified green.

### P5.7 — Local Adaptive Teaching

Deterministic/offline suggestions may use age, interests, completed skills, resume state, child-requested Help patterns and recent difficulty mix.

Allowed: suggest guided practice, skill practice through another subject, slightly greater independence/challenge, or creative/free-draw extension.

Not allowed: permanent ability labels, cloud profiling, behavioral analytics upload, grades/rank/streak punishment, forced demotion to easy content, or ML talent/quality judgement.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Must verify:
- all 24 release lessons through normal product paths;
- all four age bands;
- curriculum/journey progression;
- companion behavior;
- local adaptive teaching;
- offline/lifecycle/Gallery/recovery regressions;
- content-production release report;
- exact final APK + size/SHA + physical matrix + merged-main CI.

## Later direction after 0.5

Potential next milestones, not yet locked: Parent Zone + parent-controlled export/settings; broader accessibility/device hardening; culturally specific/folk-art curriculum after sourcing/review policy is operational; public Beta/store readiness; expand from 24 toward the 36-lesson public-V1 target; V1.0 only after product, privacy/safety, content and store-release gates pass.

## Permanent delivery rule

Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
