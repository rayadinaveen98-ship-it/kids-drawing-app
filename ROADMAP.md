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
Catalog 20→24; Content Lab 16/16; physical 36/36; merged-main CI #552 GREEN.

### P5.7 — Local Adaptive Teaching — PHYSICALLY ACCEPTED / MERGE CLOSURE ACTIVE

Issue #86. PR #87.

Delivered:
- local bounded/versioned adaptive state;
- deterministic/idempotent reducer and corruption-safe store;
- prerequisite-safe progression-aware fresh recommendations;
- explainable primary reason copy;
- coloring resume → drawing resume → fresh precedence preserved;
- browse/category/journey discovery preserved;
- child-controlled adaptive Help over authored Help/Replay only;
- no invented Trace, automatic Help, ability labels, scores/ranks or network dependency;
- deterministic cross-age pre-QA gate across all four age bands.

Accepted QA1:
- `0.5.0-curriculum-expansion-p5.7-qa1`;
- versionCode **26**;
- binary commit `e258632e83e83a39ac649855ea19592c2f5003ae`;
- Android CI #563 / run `34866700627` **GREEN**;
- profile artifact `10357367072`;
- profile SHA256 `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`;
- physical acceptance **45/45 PASS** on 2026-09-14;
- tester device/API not provided;
- no reported defects.

Remaining P5.7 gates:
1. acceptance-doc exact-head CI GREEN;
2. mark PR #87 ready;
3. squash merge;
4. merged-main CI GREEN;
5. close #86 completed.

### P5.8 — Cross-age Curriculum QA + 0.5 Release — NEXT

P5.8 is the final Phase-5 release gate. It must validate the complete integrated product, not add a parallel lesson system.

Final scope:
- all **24 lessons**;
- all four age bands;
- journeys, prerequisites and discovery;
- Companion + authored Help Ladder;
- P5.7 local adaptive recommendations and Help;
- offline / Airplane Mode;
- lifecycle, recovery and resume precedence;
- Gallery, Coloring and Free Draw;
- content quality **24 / 0 errors / six reviewed warnings** unless a documented content change deliberately changes that contract;
- permission/network/privacy boundaries;
- final versionName **`0.5.0-curriculum-expansion`**;
- new final release versionCode reserved and cut only at the final QA freeze;
- exact debug/profile/release evidence;
- focused physical acceptance;
- merge + merged-main CI before Phase 5 closes.

Completion of P5.8 produces verified **`0.5.0-curriculum-expansion`**.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.
