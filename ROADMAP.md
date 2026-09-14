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
**Locked curriculum target before adaptive/release slices:** **24 production lessons** — reached in P5.6.

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

### P5.6 — Curriculum Expansion Set E — QA1 v25 GREEN / PHYSICAL ACCEPTANCE PENDING

Issue #84. Draft PR #85. Branch `phase5/p5-6-curriculum-set-e`.

Delivered:
1. Face & Expressions — Growing/Young, D3
2. Simple Body & Pose — Growing/Young, D4
3. Create Your Character — Growing/Young, D4
4. One-Point Room — Young, D5

Catalog is now **24 production lessons**.

Verified final coverage:
- Little 8
- Creative 18
- Growing 17
- Young 10
- D1 5 / D2 9 / D3 6 / D4 3 / D5 1
- Draw With Me 23 / Watch Then Draw 17 / Trace 4

Content quality: **24 lessons / 0 errors / exactly 6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings** only for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene and One-Point Room.

Character Creator:
Face & Expressions → Simple Body & Pose → Create Your Character.

One-Point Room remains intentionally standalone with Sailboat Scene prerequisite.

Verified gates:
- contract/docs CI #529 GREEN
- Batch A 22-lesson CI #530 GREEN
- final 24-lesson CI #534 GREEN
- pre-freeze docs CI #535 GREEN
- Content Lab **16/16 PASS**
- QA1 `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode **25**
- exact QA1 head `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`
- QA1 CI #538 / run `34846235868` **GREEN**

QA1 profile artifact for physical acceptance:
- artifact `10348300909`
- APK **16,311,900 bytes**
- SHA-256 `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`

**Current gate:** exact-profile physical acceptance **36/36 PASS** using `docs/10-execution/P5_6_FOCUSED_ACCEPTANCE_CHECKLIST.md`.

After genuine 36/36 PASS:
1. commit acceptance evidence;
2. exact-head acceptance-doc CI GREEN;
3. PR #85 ready → squash merge;
4. merged-main CI GREEN;
5. close #84 and freeze P5.6.

### P5.7 — Local Adaptive Teaching — NEXT AFTER P5.6 MERGE

Deterministic/offline adaptation may use age, interests, completed skills, resume state and child-requested Help patterns. No permanent ability labels, cloud child profiling, behavioral analytics upload, grades/rank/streak punishment, forced demotion or ML talent/quality judgement.

P5.7 does **not** add a second cloud intelligence system. It adapts local recommendations/teaching choices while preserving child control and the frozen lesson/session/artwork ownership model.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Final gate across all **24 lessons**, all four age bands, journeys, Companion, local adaptive teaching, offline/lifecycle/Gallery/recovery, production content report and exact release APK evidence.

Completion of P5.8 produces the verified final `0.5.0-curriculum-expansion` milestone.

## Later direction after 0.5

Parent Zone, accessibility/device hardening, sourced/reviewed cultural curriculum, public Beta/store readiness and expansion from 24 toward 36 lessons remain later work.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
