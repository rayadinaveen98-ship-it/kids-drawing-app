# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Low-latency native drawing, editable operation history, tools, Undo/Redo/Clear, atomic persistence/recovery and deterministic teacher playback proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lesson loading/validation, all three teaching modes, Help Ladder, five paces, lifecycle/process recovery and 32/32 physical QA proven.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
`0.3.0-vertical-slice`, versionCode 13. Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — FINAL RELEASE GATE ACTIVE

**Milestone target:** `0.4.0-content-studio`  
**Epic:** #57

Goal: scale the verified product into a reusable offline multi-lesson art-learning studio without reopening proven engine foundations.

### P4.1 — Content Catalog Foundation — COMPLETE
Deterministic offline multi-package discovery, validation, typed diagnostics, metadata queries and generic catalog loading.

### P4.2 — Discovery, Art Journeys & Recommendations — COMPLETE
Multi-lesson Studio, deterministic recommendations, category/journey discovery, stable lesson identity and cross-lesson recovery.

### P4.3 — Representative Content Set A — IMPLEMENTATION MERGED / FINAL PHYSICAL COVERAGE IN P4.7
PR #67 merged at `99767b71a8e4ea20b6d587e4951c822a386019e5`; merged-main CI #342 green. Issue #60 remains open only until the final P4.7 matrix genuinely completes the deferred Set-A physical/content rows.

### P4.4 — Free Draw Studio V1 Core — COMPLETE
Issue #61 / PR #69. Squash merge `46c5954fd829e8f64cb752a58c732e895b8e3855`; merged-main CI #379 green. Previous physical result was positive smoke; all unrecorded row-level Free Draw checks are now explicit in P4.7.

### P4.5 — Coloring V1 Expansion — COMPLETE
Issue #62 / PR #70. Squash merge `10b8f2f7a6a6b4579a8695ab4f602c2e4525aec5`; merged-main CI #420 green. Focused QA1 passed; deferred process-recreation, exact Undo-boundary, offline and small-screen rows are explicit in P4.7.

### P4.6 — Representative Content Set B + Cross-content QA — COMPLETE
Issue #63 closed. PR #71 squash-merged at `b811a149e44eadfee815f1f9896f2871e9f7e25d`; merged-main Android CI #433 / run `34800596778` green.

P4.6 expanded the representative catalog to nine release lessons and added:
- Hot Air Balloon — richer guided prepared-region coloring;
- Fox Portrait — older-child detail/proportion;
- Design Your Spaceship — genuine open-ended creative variation.

### P4.7 — End-to-end QA + `0.4.0-content-studio` Release — ACTIVE
Issue #64 / draft PR #72 / branch `phase4/p4-7-final-release`.

Final candidate identity:
- versionName `0.4.0-content-studio`;
- initial versionCode 19;
- exact final binary must pass the consolidated physical release matrix before merge/delivery.

Authoritative contract/matrix:
- `docs/10-execution/P4_7_EXECUTION_CONTRACT.md`
- `docs/10-execution/P4_7_FINAL_QA.md`

The final matrix covers:
- fresh install/onboarding/profile;
- catalog/categories/Art Journeys/recommendations;
- all representative teaching modes and Set-A deferred physical coverage;
- full Free Draw tools/history/recovery/Gallery matrix;
- prepared + legacy coloring and exact history boundary;
- lesson/coloring/Free Draw lifecycle/process recreation;
- Gallery reopen/delete/source isolation;
- Airplane Mode complete core journeys;
- all four age bands plus accessibility/settings where represented;
- zero teacher/reference contamination, no line-art damage or operation loss;
- no crash/ANR/deadlock;
- exact-head and merged-main CI, reproducible profile APK size/SHA, final release report and delivered APK.

If any changed candidate APK is distributed after versionCode 19, the next binary must increment versionCode monotonically.

Phase 4 closes only after the final candidate passes the physical matrix, acceptance evidence is committed, PR #72 is clean/exact-head green, merged-main CI is green, remaining issue #60 is truthfully resolved, issue #64 and epic #57 are closed, and the exact final APK is delivered.

## Post-Phase-4 direction
- companion expression/voice polish;
- expand representative curriculum toward public V1 24–36 lessons;
- adaptive local recommendations/help without punitive scoring;
- Parent Zone / parent-controlled export/settings;
- broader accessibility/device hardening and Beta;
- V1.0 only after product, privacy/safety, content and store-release gates pass.

## Permanent delivery rule
Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
