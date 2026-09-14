# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Core native drawing and editable history/recovery proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lessons, all three teaching modes, Help Ladder and recovery proven; physical 32/32 PASS.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — COMPLETE
`0.4.0-content-studio`, versionCode 19 remains the latest fully released milestone.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — ACTIVE

**Target:** `0.5.0-curriculum-expansion`  
**Parent epic:** #73  
**Final curriculum target before adaptive/release slices:** 24 production lessons

### P5.1 — Curriculum & Teaching Contract — COMPLETE
Age 4–12 progression, content allocation, quality rubric, journey map and authorship philosophy locked.

### P5.2 — Content Production System V2 — COMPLETE
Analyzer, Content Lab, geometry/help/coloring checks and release reporting proven.

### P5.3 — Companion / Teacher Experience V2 — COMPLETE
Generic deterministic age-aware Companion V2 proven; physical 20/20 PASS.

### P5.4 — Curriculum Expansion Set C — COMPLETE
Catalog 9 → 14. Content Lab 15/15 and physical 30/30 PASS. Merged-main CI #514 GREEN.

### P5.5 — Curriculum Expansion Set D — COMPLETE
Issue #82 closed. PR #83 squash-merged at `6412e0e6cf346837b26e925cebc89662c27fba2c`. Merged-main Android CI #528 GREEN.

Delivered Snail Garden, Elephant From Shapes, Simple Car, Sailboat Scene, Planet With Rings and Friendly Alien. Catalog 14 → **20**.

Accepted QA1 v24:
- quality 20 / 0 errors / 5 reviewed standalone warnings;
- Content Lab 18/18 PASS;
- exact-profile physical 36/36 PASS;
- profile artifact `10344519403`, APK 16,293,898 bytes, SHA-256 `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`.

### P5.6 — Curriculum Expansion Set E — ACTIVE / CONTRACT LOCKED

Issue #84. Branch `phase5/p5-6-curriculum-set-e`.

Verified start: main `6412e0e6cf346837b26e925cebc89662c27fba2c`, merged-main CI #528 GREEN.

Execution contract `docs/10-execution/P5_6_EXECUTION_CONTRACT.md` was committed first at `f11f2a6dc857369651ee8a5cb33d95532736bd2b`, before content implementation.

Set E:
1. Face & Expressions — Growing/Young, D3
2. Simple Body & Pose — Growing/Young, D4
3. Create Your Character — Growing/Young, D4
4. One-Point Room — Young, D5

Character Creator:
Face & Expressions → Simple Body & Pose → Create Your Character.

One-Point Room remains standalone with Sailboat Scene prerequisite.

Target catalog: **20 → 24**.

Expected final age coverage:
- Little 8
- Creative 18
- Growing 17
- Young 10

Expected final difficulty distribution:
- D1 5
- D2 9
- D3 6
- D4 3
- D5 1

Final quality policy: 24 lessons / 0 errors / exactly six reviewed standalone warnings only for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene and One-Point Room.

Planned gates:
1. contract/docs + draft PR + CI;
2. Batch A Face + Body → 22 lessons + CI;
3. Batch B Character + Room → 24 lessons + CI;
4. Content Lab;
5. QA1 v25 immutable artifact evidence;
6. exact-profile physical acceptance;
7. acceptance CI → squash merge → merged-main CI → close #84.

### P5.7 — Local Adaptive Teaching

After P5.6 only. Deterministic/offline suggestions may use age, interests, completed skills, resume state and child-requested Help patterns. No permanent ability labels, cloud profiling, grades/rank/streak punishment or ML talent judgement.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Final gate across all 24 lessons, four age bands, journeys, Companion, local adaptive teaching, offline/lifecycle/Gallery/recovery, production content report and exact release APK evidence.

## Later direction after 0.5

Parent Zone, accessibility/device hardening, sourced/reviewed cultural curriculum, public Beta/store readiness, and expansion from 24 toward 36 lessons remain later work.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
