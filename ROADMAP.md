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
**Frozen curriculum:** **24 production lessons**.

### P5.1 — Curriculum & Teaching Contract — COMPLETE

### P5.2 — Content Production System V2 — COMPLETE

### P5.3 — Companion / Teacher Experience V2 — COMPLETE
Physical 20/20 PASS.

### P5.4 — Curriculum Expansion Set C — COMPLETE
Catalog 9→14; Content Lab 15/15; physical 30/30.

### P5.5 — Curriculum Expansion Set D — COMPLETE
Catalog 14→20; Content Lab 18/18; physical 36/36; merged-main CI #528 GREEN.

### P5.6 — Curriculum Expansion Set E — COMPLETE / FROZEN

Catalog 20→24.

Accepted gates:
- final curriculum 24 lessons / 0 errors / six reviewed standalone warnings;
- Content Lab **16/16 PASS**;
- QA1 `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode 25;
- exact-profile physical **36/36 PASS**;
- PR #85 squash-merged at `91bc7584994224852d3d44749e78749d39ff954b`;
- merged-main Android CI #552 / run `34849748403` **GREEN**;
- issue #84 closed completed.

### P5.7 — Local Adaptive Teaching — ACTIVE

Issue #86. Branch `phase5/p5-7-local-adaptive-teaching`.

Contract-first commit: `5225e82bcae4afae7a0d5dc65bacd1a5054d4b19`.

Goal: extend the existing local Home recommendation + authored Help presentation with deterministic, bounded, explainable adaptation while preserving child control and all existing ownership boundaries.

Architecture direction:
- owned advisory layer under `product/adaptive`;
- local/versioned/corruption-tolerant state only;
- deterministic idempotent reducer from genuine completion and child-requested Help events;
- `StudioPrimarySelectionPolicy` resume precedence remains coloring → drawing → fresh suggestion;
- `StudioRecommendationPolicy` remains the Home recommendation surface;
- fresh adaptive primary suggestions respect prerequisites and exact age fit;
- journey continuation, underexposed/new skills, explicit interests, mode preference, sensible difficulty context and bounded recent-repeat avoidance may influence deterministic ordering;
- recommendation reasons remain human-readable policy facts, not ability scores;
- Help adaptation may only suggest existing authored Help/replay **after the child asks**.

Forbidden:
- raw stroke/artwork quality analysis;
- permanent ability labels;
- score/grade/rank/XP/punitive streaks;
- cloud child profiling/analytics upload;
- network dependence;
- forced demotion;
- automatic Help escalation;
- lesson-ID-specific adaptive branches.

Delivery gates:
1. contract/docs CI GREEN;
2. Batch A adaptive state/store/reducer → CI GREEN;
3. Batch B progression-aware fresh recommendations → CI GREEN;
4. Batch C child-controlled adaptive Help → CI GREEN;
5. deterministic/offline/corruption/privacy QA;
6. QA1 freeze `0.5.0-curriculum-expansion-p5.7-qa1`, versionCode **26**;
7. exact APK evidence + physical acceptance;
8. acceptance CI → merge → merged-main CI → close #86.

VersionCode 26 is not used before QA freeze.

### P5.8 — Cross-age Curriculum QA + 0.5 Release

Final gate across all 24 lessons, all four age bands, journeys, Companion, local adaptive teaching, offline/lifecycle/Gallery/recovery, content-quality report and exact final release APK evidence.

Completion of P5.8 produces verified `0.5.0-curriculum-expansion`.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
