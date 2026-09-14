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
**Locked curriculum target:** **24 production lessons** — reached in P5.6.

### P5.1 — Curriculum & Teaching Contract — COMPLETE

### P5.2 — Content Production System V2 — COMPLETE

### P5.3 — Companion / Teacher Experience V2 — COMPLETE
Physical 20/20 PASS.

### P5.4 — Curriculum Expansion Set C — COMPLETE
Catalog 9 → 14; Content Lab 15/15; physical 30/30.

### P5.5 — Curriculum Expansion Set D — COMPLETE
Catalog 14 → 20; merged-main CI #528 GREEN; Content Lab 18/18; physical 36/36.

### P5.6 — Curriculum Expansion Set E — PHYSICAL ACCEPTANCE PASSED / FINAL MERGE GATE ACTIVE

Issue #84 / PR #85.

Delivered:
1. Face & Expressions — Growing/Young, D3
2. Simple Body & Pose — Growing/Young, D4
3. Create Your Character — Growing/Young, D4
4. One-Point Room — Young, D5

Verified curriculum:
- **24 lessons / 0 errors / exactly 6 reviewed standalone warnings**
- Little 8 / Creative 18 / Growing 17 / Young 10
- D1 5 / D2 9 / D3 6 / D4 3 / D5 1
- DWM 23 / WTD 17 / Trace 4

Verified gates:
- final 24-lesson CI #534 GREEN
- pre-freeze docs CI #535 GREEN
- Content Lab **16/16 PASS**
- QA1 v25 exact binary head `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`
- QA1 CI #538 GREEN
- accepted profile artifact `10348300909`, 16,311,900 bytes, SHA-256 `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`
- physical acceptance **36/36 PASS** reported on 2026-09-14
- device/API not provided and not inferred

Remaining P5.6 closure:
1. acceptance evidence exact-head CI GREEN;
2. PR #85 ready → squash merge;
3. merged-main CI GREEN;
4. close #84 completed and freeze P5.6.

### P5.7 — Local Adaptive Teaching — NEXT

Starts only from verified post-P5.6 `main`.

Goal: add deterministic, offline adaptation that improves what the child sees next and how help is presented without becoming a grading or profiling system.

Allowed local signals:
- age band;
- explicit interests/preferences;
- completed lessons/skills;
- resume/in-progress state;
- child-requested Help usage patterns;
- recent deterministic lesson history needed for recommendations.

Forbidden:
- permanent ability labels;
- cloud child profiling or behavioral analytics upload;
- similarity scoring or ML talent/quality judgment;
- grades/rank/XP/punitive streaks;
- forced demotion or hidden punishment;
- overriding child choice or mutating lesson/session/artwork truth.

P5.7 should produce transparent, testable recommendation/help policies behind owned interfaces, work fully offline, and remain deterministic for the same local state.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Final gate across all 24 lessons, four age bands, journeys, Companion, local adaptive teaching, offline/lifecycle/Gallery/recovery, production content report and exact release APK evidence.

Completion of P5.8 produces the verified `0.5.0-curriculum-expansion` milestone.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
