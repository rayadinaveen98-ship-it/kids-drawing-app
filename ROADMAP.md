# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lessons, all teaching modes, Help Ladder and recovery proven; physical 32/32 PASS.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
Production onboarding → lesson → coloring → Gallery journey physically passed 41/41.

## Phase 4 — Content & Studio Expansion 0.4 — COMPLETE
`0.4.0-content-studio`, versionCode 19 remains the latest fully released milestone.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — ACTIVE

**Target:** `0.5.0-curriculum-expansion`  
**Parent epic:** #73  
**Locked curriculum target before adaptive/release slices:** 24 production lessons.

### P5.1 — Curriculum & Teaching Contract — COMPLETE
Age 4–12 progression, lesson allocation, quality rubric, journey map and authorship philosophy locked.

### P5.2 — Content Production System V2 — COMPLETE
Analyzer, Content Lab, geometry/help/coloring checks and release reporting proven.

### P5.3 — Companion / Teacher Experience V2 — COMPLETE
Generic deterministic age-aware Companion V2 proven; physical 20/20 PASS.

### P5.4 — Curriculum Expansion Set C — COMPLETE
Catalog 9 → 14. Content Lab 15/15, physical 30/30, merged-main CI #514 GREEN.

### P5.5 — Curriculum Expansion Set D — COMPLETE
Catalog 14 → 20. PR #83 squash-merged at `6412e0e6cf346837b26e925cebc89662c27fba2c`; merged-main CI #528 GREEN. Accepted QA1 v24: 20 / 0 errors / 5 reviewed warnings; Content Lab 18/18; physical 36/36.

### P5.6 — Curriculum Expansion Set E — IMPLEMENTED / AUTOMATED FINAL CONTENT GATE GREEN / PRE-FREEZE CONTENT LAB PENDING

Issue #84. Draft PR #85. Branch `phase5/p5-6-curriculum-set-e`.

Contract-first commit `f11f2a6dc857369651ee8a5cb33d95532736bd2b`; CI #529 GREEN.

Delivered:
1. Face & Expressions — Growing/Young, D3
2. Simple Body & Pose — Growing/Young, D4
3. Create Your Character — Growing/Young, D4
4. One-Point Room — Young, D5

Implementation checkpoints:
- Batch A Face + Body `fd44ab754e84191d3934f373c1dac05bcca51d1b`; CI #530 GREEN.
- Batch B Character + Room `0393274a8d17290c88a49e9a383d155030fefcae`.
- historical analyzer test finalized at `9244e776707826e613a0bec43a22f60edaae3ed0`.
- exact-head CI #534 GREEN.

Catalog is now **24 production lessons**.

Final verified coverage:
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

Character Creator:
Face & Expressions → Simple Body & Pose → Create Your Character.

One-Point Room remains intentionally standalone with Sailboat Scene prerequisite.

Final content-quality checkpoint:
- 24 lessons
- 0 errors
- exactly 6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings only for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene and One-Point Room
- all Phase-5 lesson/age/difficulty/Watch-Then-Draw target flags met.

**Pre-freeze gate:** Content Lab all four Set-E packages must pass **16/16** before the first distributed P5.6 QA candidate is cut.

Engineering-only pre-freeze artifact from CI #534:
- profile artifact `10346199935`
- APK 16,311,900 bytes
- SHA-256 `11d9459cbeefff3ff26a457b7a28b7e082ce08b55c8c8b70ce546444e24b4867`
- this binary is not P5.6 QA1; inherited P5.5 version/artifact naming is intentionally retained until the pre-freeze Content Lab gate passes.

After Content Lab 16/16:
1. freeze `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode 25;
2. exact QA1 CI + immutable artifact evidence;
3. focused physical acceptance;
4. acceptance CI → PR ready → squash merge → merged-main CI → close #84.

### P5.7 — Local Adaptive Teaching

Starts only after P5.6 is accepted/merged. Deterministic/offline suggestions may use age, interests, completed skills, resume state and child-requested Help patterns. No permanent ability labels, cloud profiling, behavioral analytics upload, grades/rank/streak punishment, forced demotion or ML talent/quality judgment.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Final gate across all 24 lessons, four age bands, journeys, Companion, local adaptive teaching, offline/lifecycle/Gallery/recovery, production content report and exact release APK evidence.

## Later direction after 0.5

Parent Zone, accessibility/device hardening, sourced/reviewed cultural curriculum, public Beta/store readiness and expansion from 24 toward 36 lessons remain later work.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
