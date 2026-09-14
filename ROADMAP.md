# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lessons, all teaching modes, Help Ladder and recovery proven; physical 32/32 PASS.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
Production onboarding → lesson → coloring → Gallery journey physically passed 41/41.

## Phase 4 — Content & Studio Expansion 0.4 — COMPLETE
`0.4.0-content-studio`, versionCode 19 remains the latest fully released milestone until Phase 5 closes.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — ACTIVE / FINAL SLICE

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
Catalog 20→24; Content Lab 16/16; physical 36/36; merged-main CI #552 GREEN.

### P5.7 — Local Adaptive Teaching — COMPLETE / FROZEN

- issue #86 closed completed;
- PR #87 squash-merged at `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`;
- merged-main Android CI #571 / run `34869966116` GREEN;
- QA1 `0.5.0-curriculum-expansion-p5.7-qa1`, versionCode 26;
- physical acceptance 45/45 PASS;
- local bounded/versioned adaptive state;
- deterministic prerequisite-safe explainable fresh recommendations;
- coloring resume → drawing resume → fresh precedence preserved;
- child-controlled adaptive Help over authored Help/Replay only;
- no invented Trace, automatic Help, ability labels, scores/ranks or network dependency.

### P5.8 — Cross-age Curriculum QA + 0.5 Release — ACTIVE

Issue #88. Branch `phase5/p5-8-final-release`.

P5.8 is the final Phase-5 integrated release gate. It validates the complete accepted product rather than adding a new feature system.

Final scope:
- all **24 lessons**;
- all four age bands;
- journeys, prerequisites and discovery;
- Draw With Me / Watch Then Draw / authored Trace;
- Companion + authored Help Ladder + Replay;
- P5.7 local adaptive recommendations and child-controlled adaptive Help;
- offline / Airplane Mode;
- lifecycle, recovery and resume precedence;
- Gallery, Coloring and Free Draw;
- content quality **24 / 0 errors / six reviewed warnings**;
- permission/network/privacy boundaries;
- final versionName **`0.5.0-curriculum-expansion`**;
- final versionCode **27 reserved and cut only at final QA freeze**;
- exact debug/profile evidence;
- focused physical acceptance;
- acceptance-doc CI + squash merge + merged-main CI before Phase 5 closes.

Contract-first commit:
- `0bb87cb845b9ae713a4cefcf46b55a57fdce6148`.

Pre-freeze order:
1. contract/docs CI GREEN;
2. integrated cross-age Phase-5 regression gate GREEN;
3. only then cut final versionCode 27 candidate;
4. exact artifact evidence + final physical acceptance;
5. merge + merged-main CI;
6. mark Phase 5 COMPLETE and make `0.5.0-curriculum-expansion` the latest fully verified milestone.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
