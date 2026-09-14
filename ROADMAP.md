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

Key contract:
- increase creative authorship as age/difficulty rises;
- no similarity grading, rankings or permanent ability labels;
- child-controlled Help remains non-punitive;
- older-child content must include credible proportion/perspective/character/composition technique;
- optional lightweight reflection/connection prompts can enrich learning without school-style assessment;
- culturally specific content requires respectful sourcing/context/review.

Phase-5 exact new lesson target:
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

Issue #76 closed completed. PR #77 squash-merged at `c0e4c3708fd94102773d6438cbf401162815a9fc`; merged-main CI #482 / run `34809175064` GREEN.

Accepted QA2:
- `0.5.0-curriculum-expansion-p5.2-qa2`, versionCode 21;
- executable `2f36834d110cb1076b953f519eee4a8dc6e2e19d`;
- CI #477 GREEN;
- profile artifact `10333593024`;
- APK size 16,245,548 bytes;
- SHA-256 `725720ae1fbff232cbb56049d77b087d8214f959189269630dbbd9eeb0817cf6`;
- Content Lab focused matrix 20/20 PASS;
- final acceptance-doc CI #481 GREEN;
- merged-main CI #482 GREEN.

### P5.3 — Companion / Teacher Experience V2 — COMPLETE

Issue #78 closed completed. PR #79 squash-merged at `e3553414c591ae5def3d9016c1a63e9d1a350f39`; merged-main CI #498 GREEN.

Delivered:
- deterministic age-aware companion policy for Little / Creative / Growing / Young;
- clearer teacher-turn, child-turn, Help, pause, completion and error presentation;
- Watch Then Draw overview vs per-step distinction;
- generic open-ended authorship detection without lesson-ID branches;
- non-punitive Help/Trace language;
- deterministic context cues for Replay/Watch/Trace/open choice;
- optional non-blocking completion reflection;
- Guided Lesson integration using the existing product profile age band;
- secondary cue rendering inside companion card;
- legacy `Cute Cat` / `save your cat` generic fallback copy removed;
- control/session semantics preserved.

Accepted QA1:
- `0.5.0-curriculum-expansion-p5.3-qa1`, versionCode 22;
- physically tested executable `eef87b25478c6a30d6fefbd7580f75ded4eca3ab`;
- exact frozen QA1 CI #493 GREEN;
- profile artifact `10334873551`;
- profile APK 16,245,553 bytes;
- SHA-256 `bed4c00bce4629822b50c6523527a1ebad66428fc0977580501a20c77ad3e5de`;
- focused physical/product matrix **20/20 PASS**;
- final merge gates completed and issue #78 closed.

### P5.4 — Curriculum Expansion Set C — ACTIVE / AUTOMATED QA1 PASS

Issue #80, draft PR #81, branch `phase5/p5-4-curriculum-set-c`.

Delivered Set C:
- Happy Lines;
- Shape Friends;
- Rainbow Weather;
- Tree Through Seasons;
- Ice Cream Shop.

Catalog is now **14 production release lessons**.

Automated QA1 candidate:
- `0.5.0-curriculum-expansion-p5.4-qa1`, versionCode 23;
- exact QA commit `797d2219c4fe7f643d31f1ada42e08bacf7d105f`;
- complete Set-C CI #508 GREEN;
- exact frozen QA1 CI #509 / run `34831113980` GREEN;
- profile artifact `10342178179`;
- profile APK 16,267,569 bytes;
- profile SHA-256 `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`;
- debug artifact `10341973952`;
- content-quality artifact `10342606597`;
- content report 14 lessons / 0 errors / 3 reviewed `NO_JOURNEY_MEMBERSHIP` warnings;
- permission allowlist green.

P5.4 is **not complete yet**. Remaining required gates:
- interactive Content Lab visual inspection of all five Set-C lessons;
- focused physical-device pass on exact profile artifact `10342178179`;
- acceptance evidence synchronization;
- final exact-head acceptance-doc CI;
- PR #81 ready/squash merge;
- merged-main CI;
- issue #80 close completed.

### P5.5 — Curriculum Expansion Set D

Animals + vehicles + space. Planned lesson pool includes Snail Garden, Elephant From Shapes, Simple Car, Sailboat Scene, Planet With Rings and Friendly Alien.

### P5.6 — Curriculum Expansion Set E

People/characters + older-child technique. Planned lesson pool includes Face & Expressions, Simple Body & Pose, Create Your Character and One-Point Room.

Batching between P5.4–P5.6 may be balanced for QA efficiency, but the exact 24-lesson target must not silently drift.

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
