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

Released milestone: `0.4.0-content-studio` / versionCode 19.  
Parent epic #57 closed. Final PR #72 squash-merged at `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`. Merged-main CI #446 GREEN; closure-doc CI #450 GREEN.

Phase 4 proved the reusable offline product foundation:
- nine production lessons;
- multi-lesson Studio/categories/Art Journeys;
- deterministic recommendations and safe resume;
- Trace & Learn / Draw With Me / Watch Then Draw;
- Help Ladder and grouped demos;
- Free Draw;
- prepared + legacy coloring;
- lifecycle/recovery;
- Gallery safety;
- Airplane Mode core journeys;
- cross-age final physical QA.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — ACTIVE

**Target:** `0.5.0-curriculum-expansion`  
**Parent epic:** #73  
**Latest fully verified product milestone:** `0.4.0-content-studio`, versionCode 19

### Milestone strategy

Phase 5 turns the technically proven 0.4 app into a deeper art-learning product without reopening proven engines by default.

Locked catalog target for 0.5:
- 24 total production guided lessons;
- 9 existing verified lessons;
- 15 new lessons;
- 36 remains the later public-V1 target after the 24-lesson workflow/content system is proven.

### P5.1 — Curriculum & Teaching Contract — COMPLETE

Issue #74 closed completed. PR #75 squash-merged at `cea06e219290c82b9a1f8f8007069c61841bbc95`. Exact-head CI #451 GREEN; merged-main CI #452 GREEN.

Authoritative research/contract:
- `docs/10-execution/P5_1_CURRICULUM_RESEARCH.md`
- `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`

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

### P5.2 — Content Production System V2 — ACTIVE / QA2

Issue #76, draft PR #77, branch `phase5/p5-2-content-production-v2`.

Purpose: build scalable authoring validation/inspection before adding 15 lessons. The system reuses production lesson/catalog contracts and is not a second runtime.

Implemented:
- deterministic catalog coverage/readiness report in text + JSON;
- release errors projected from production catalog/package validation;
- Phase-5 target progress for lesson/age/difficulty/mode coverage;
- conservative authoring warnings for suspicious step/geometry/reference/help/prepared-region patterns;
- deliberate negative JVM fixtures;
- CI report gate + GitHub step summary + report artifact;
- calibrated existing nine-lesson baseline = **0 errors, 0 warnings**;
- read-only `ContentInspectionRepository`;
- engineering-only `ContentLabActivity` showing real bundled preview/thumbnail SVG, lesson metadata, step/help/coloring contract, teacher/expected/help/prepared-region geometry and localization;
- Content Lab has no child document/session mutation path and is not part of the child-facing launcher flow.

Automated proof before QA freeze:
- full implementation CI #463 GREEN;
- calibrated baseline CI #465 / run `34806642996` GREEN;
- calibrated report artifact `10333610875`;
- report: 9 lessons, 0 errors, 0 warnings, Phase-5 catalog progress 9/24.

QA candidate history:
- **QA1 / versionCode 20:** frozen at `07025d8cf35569179b1ee1e9443303bd92019093`; CI #471 failed because the freeze edit accidentally removed existing Compose UI test dependencies; no QA1 APK was distributed or accepted.
- **QA2 / versionCode 21:** `0.5.0-curriculum-expansion-p5.2-qa2`; test dependencies restored; dedicated QA2 artifact naming configured; exact frozen head/CI/artifact evidence pending.

QA matrix: `docs/10-execution/P5_2_QA.md`.

P5.2 exit: exact QA2 CI + reproducible APK/report evidence → focused device pass → acceptance-doc CI → PR #77 merge → merged-main CI → #76 close.

Bulk P5.4–P5.6 content production remains blocked until this exit target is complete.

### P5.3 — Companion / Teacher Experience V2 — NEXT AFTER P5.2

Improve the patient-teacher presentation while preserving autonomy:
- clearer teacher-turn vs child-turn state;
- context-aware encouragement;
- age-appropriate tone;
- optional reflection/creative-extension prompts;
- Help presentation tuned by age/difficulty;
- celebration without scoring/pressure.

### P5.4 — Curriculum Expansion Set C

Foundations + early-child/nature/everyday content. Planned lesson pool includes Happy Lines, Shape Friends, Rainbow Weather, Tree Through Seasons and Ice Cream Shop, balanced against QA scope.

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

Potential next milestones, not yet locked:
- Parent Zone + parent-controlled export/settings;
- broader accessibility/device hardening;
- culturally specific/folk-art curriculum after sourcing/review policy is operational;
- public Beta/store readiness;
- expand from 24 toward the 36-lesson public-V1 target;
- V1.0 only after product, privacy/safety, content and store-release gates pass.

## Permanent delivery rule

Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
