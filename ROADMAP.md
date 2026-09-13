# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Low-latency native drawing, editable operation history, tools, Undo/Redo/Clear, atomic persistence/recovery and deterministic teacher playback proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lesson loading/validation, all three teaching modes, Help Ladder, five paces, lifecycle/process recovery and 32/32 physical QA proven.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
`0.3.0-vertical-slice`, versionCode 13. Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — ACTIVE

**Milestone target:** `0.4.0-content-studio`  
**Epic:** #57

Goal: scale the verified product into a reusable offline multi-lesson art-learning studio without reopening proven engine foundations.

### P4.1 — Content Catalog Foundation — COMPLETE
Deterministic offline multi-package discovery, validation, typed diagnostics, metadata queries and generic catalog loading.

### P4.2 — Discovery, Art Journeys & Recommendations — COMPLETE
Multi-lesson Studio, deterministic recommendations, category/journey discovery, stable lesson identity and cross-lesson recovery.

### P4.3 — Representative Content Set A — IMPLEMENTATION MERGED / PHYSICAL MATRIX OPEN
Smiling Sun, Friendly Owl, Simple Rocket and Easy Flower are integrated. PR #67 merged at `99767b71a8e4ea20b6d587e4951c822a386019e5`; merged-main CI #342 green. Issue #60 remains open because its broader physical/content matrix was not fully rerun. Finish that coverage no later than P4.7.

### P4.4 — Free Draw Studio V1 Core — COMPLETE
Issue #61 / PR #69. Squash merge `46c5954fd829e8f64cb752a58c732e895b8e3855`; merged-main CI #379 green.

Delivered:
- production blank/recovered Free Draw canvas;
- age-adaptive Pencil/Crayon/Marker/Eraser controls;
- palette/size, Undo/Redo, confirmation-only undoable Clear;
- Save & leave / lifecycle recovery;
- explicit Free Draw Gallery provenance and safe reopen/delete;
- working-document isolation and responsive tool layout.

QA1 versionCode 16 received positive user physical smoke. Exhaustive deferred P4.4 rows remain part of P4.7 regression.

### P4.5 — Coloring V1 Expansion — ACCEPTED / MERGE VERIFICATION PENDING
Issue #62 / PR #70 / branch `phase4/p4-5-coloring-expansion`.

Delivered:
- content API 2 prepared coloring regions with strict package validation and API-1 compatibility;
- legacy empty-region freehand coloring preserved for Cute Cat r1;
- DrawingDocument schema 3 prepared-fill operation with backward-readable schema 1/2 documents;
- reversible Fill Undo/Redo and recoloring;
- prepared fill isolated to color projection beneath protected line art;
- Gallery previews render fills beneath line art;
- authored Color With Me progression derived from lesson steps and document operations;
- Fill offered only for valid prepared-region content/current guided step;
- Compose Fill tap overlay uses the shared logical-coordinate mapper while AndroidX Ink Brush/Eraser input remains untouched;
- child-facing coloring copy is content/progress-driven rather than Cute-Cat/stroke-count hardcoding;
- production lesson `little-fish` r1 proves guided prepared coloring with body, tail and fin regions.

QA1 evidence:
- `0.4.0-content-studio-p4.5-qa1`, versionCode 17;
- executable `240007b6161ebfefd09252efa844e4d18808a7f0`;
- CI #415 / run `34756548584` green;
- profile artifact `10317956245`;
- size `16,183,450 bytes`;
- SHA-256 `cf45fedc129523c8c9d3784e3d6ea70d0319dc1b8f9236430b85c099075394d6`;
- supplied focused physical/product checklist user-reported PASS on 2026-09-13.

Deferred—not falsely marked physical PASS—until P4.7: process-death-specific prepared-coloring sequence, exact coloring-undo boundary sequence, airplane mode and small-screen-specific coverage.

Remaining gate: exact-head CI after acceptance documentation → ready/merge PR #70 → merged-main CI → close #62.

### P4.6 — Representative Content Set B + Cross-content QA — NEXT
Issue #63. Branch only from verified P4.5 `main` merge.

Target:
- add older-child detail/proportion lesson;
- add open-ended creative-variation lesson;
- expand guided-coloring representative coverage where needed;
- reach at least seven production-quality representative lessons across ages, categories, skills and difficulty;
- validate cross-content routing, recommendations, journeys, help, preview, recovery, coloring/free-draw coexistence and generic no-ID-special-case behavior.

### P4.7 — End-to-end QA + `0.4.0-content-studio` Release
Full offline/lifecycle/accessibility/device regression across catalog discovery, guided lessons, Free Draw, expanded coloring and Gallery. Complete deferred P4.3/P4.4/P4.5 physical coverage, then deliver the verified final `0.4.0-content-studio` APK with reproducible evidence.

## Post-Phase-4 direction
- companion expression/voice polish;
- expand representative curriculum toward public V1 24–36 lessons;
- adaptive local recommendations/help without punitive scoring;
- Parent Zone / parent-controlled export/settings;
- broader accessibility/device hardening and Beta;
- V1.0 only after product, privacy/safety, content and store-release gates pass.

## Permanent delivery rule
Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
