# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Low-latency native drawing, editable operation history, tools, Undo/Redo/Clear, atomic persistence/recovery and deterministic teacher playback proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lesson loading/validation, all three teaching modes, Help Ladder, five paces, lifecycle/process recovery and 32/32 physical QA proven.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
`0.3.0-vertical-slice`, versionCode 13. Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — FINAL REPOSITORY CLOSURE

**Milestone target:** `0.4.0-content-studio`  
**Epic:** #57

Goal: scale the verified product into a reusable offline multi-lesson art-learning studio without reopening proven engine foundations.

### P4.1 — Content Catalog Foundation — COMPLETE
Deterministic offline multi-package discovery, validation, typed diagnostics, metadata queries and generic catalog loading.

### P4.2 — Discovery, Art Journeys & Recommendations — COMPLETE
Multi-lesson Studio, deterministic recommendations, category/journey discovery, stable lesson identity and cross-lesson recovery.

### P4.3 — Representative Content Set A — PHYSICAL DEFERRED ROWS NOW PASS
PR #67 merged at `99767b71a8e4ea20b6d587e4951c822a386019e5`; merged-main CI #342 GREEN. The final P4.7 exact-candidate checklist passed Smiling Sun, Friendly Owl, Simple Rocket, Easy Flower, Help/replay/cumulative construction and Gallery regression. Issue #60 may close after final acceptance-doc CI.

### P4.4 — Free Draw Studio V1 Core — COMPLETE
Issue #61 / PR #69. Merge `46c5954fd829e8f64cb752a58c732e895b8e3855`; merged-main CI #379 GREEN. Full deferred Free Draw row-level physical matrix passed in the exact P4.7 candidate.

### P4.5 — Coloring V1 Expansion — COMPLETE
Issue #62 / PR #70. Merge `10b8f2f7a6a6b4579a8695ab4f602c2e4525aec5`; merged-main CI #420 GREEN. Deferred process recreation, exact Undo boundary, offline and accessibility-related coloring checks passed in P4.7.

### P4.6 — Representative Content Set B + Cross-content QA — COMPLETE
Issue #63 closed. PR #71 merge `b811a149e44eadfee815f1f9896f2871e9f7e25d`; merged-main Android CI #433 GREEN. Nine representative release lessons are present across all four age bands and difficulty 1–4.

### P4.7 — End-to-end QA + `0.4.0-content-studio` Release — FINAL CANDIDATE PASS
Issue #64 / draft PR #72 / branch `phase4/p4-7-final-release`.

Accepted release candidate:
- versionName `0.4.0-content-studio`;
- versionCode 19;
- executable `f3843365d39540de00fe08a008883c15abe75599`;
- Android CI #440 / run `34801122118` GREEN;
- profile artifact `10331363240`;
- profile APK size `16,196,353 bytes`;
- profile SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`;
- independent local hash/size verification PASS;
- complete supplied final physical/product checklist user-reported PASS on 2026-09-14.

The final checklist closed the deferred Phase-4 evidence for:
- fresh install/onboarding/profile/catalog/categories/journeys/recommendations;
- Trace & Learn, Draw With Me, Watch Then Draw, Help Ladder and grouped playback;
- Hot Air Balloon/Fox Portrait/Design Your Spaceship;
- full Free Draw tools/history/recovery/Gallery matrix;
- prepared + legacy coloring, exact Undo boundary and protected line art;
- lesson/coloring/Free Draw lifecycle + force-stop recovery;
- Gallery reopen/delete/source isolation;
- complete Airplane Mode core journeys;
- age 4–5 / 6–7 / 8–9 / 10–12 and larger font-scale spot checks;
- contamination, operation durability and stability sweep.

Conditional rows for handedness, voice-off and reduced-motion are N/A because those product controls are not exposed in this milestone. Resume-priority policy and missing/incompatible content fallback remain backed by frozen automated tests rather than fabricated device failure injection.

No release-blocking defect was found, so v19 remains the accepted binary. Any future changed APK must increment versionCode.

Remaining closure only:
1. exact-head CI on final acceptance documentation;
2. close P4.3 #60;
3. ready/merge PR #72;
4. merged-main CI green;
5. close P4.7 #64 and epic #57;
6. record final repository closure evidence while preserving the exact physically accepted binary above.

No `v0.4.0-content-studio` tag is claimed because connected tooling does not expose tag creation.

## Post-Phase-4 direction
- companion expression/voice polish;
- expand representative curriculum toward public V1 24–36 lessons;
- adaptive local recommendations/help without punitive scoring;
- Parent Zone / parent-controlled export/settings;
- broader accessibility/device hardening and Beta;
- V1.0 only after product, privacy/safety, content and store-release gates pass.

## Permanent delivery rule
Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
