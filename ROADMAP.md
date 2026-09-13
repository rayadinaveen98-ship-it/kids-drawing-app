# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE

Product scope, UX architecture, lesson/content architecture, Drawing/Lesson/Coloring contracts, companion/visual direction, safety, quality and release strategy are locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE

Milestone `0.1.0-art-lab`.

Proven baseline: low-latency native drawing, editable operation history, Pencil/Eraser/color/width, Undo/Redo/Clear, atomic persistence/recovery, deterministic teacher playback at five paces, Art/Quality labs and physical quality evidence.

## Phase 2 — Lesson Engine 0.2 — COMPLETE

Milestone `0.2.0-lesson-engine`.

Proven: structured lesson loading/validation, deterministic lesson state/snapshots, Draw With Me, Watch Then Draw, Trace & Learn, Help Ladder, all five paces, lifecycle/process recovery, strict overlay isolation and 32/32 physical QA.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE

Milestone `0.3.0-vertical-slice`, versionCode 13.

Proven production journey:
onboarding → profile/age preferences → Studio recommendation → lesson preview/companion → guided drawing/help → safe coloring handoff → completion → Gallery/reopen.

P3.6 final executable candidate passed 41/41 physical scenarios. Evidence lives in `docs/10-execution/P3_6_PHYSICAL_QA.md` and `docs/10-execution/P3_6_RELEASE_REPORT.md`.

## Phase 4 — Content & Studio Expansion 0.4 — ACTIVE

**Milestone target:** `0.4.0-content-studio`  
**Epic:** #57

Goal: scale the verified product beyond one Cute Cat lesson without reopening the frozen engine architecture.

### P4.1 — Content Catalog Foundation — COMPLETE
Deterministic offline multi-package discovery, validation, typed diagnostics, metadata queries and generic catalog loading.

### P4.2 — Discovery, Art Journeys & Recommendations — COMPLETE
Child-facing multi-lesson Studio, category/journey browsing, deterministic age/interest/mode ranking, resume precedence and stable lesson-specific routing/recovery.

### P4.3 — Representative Content Set A — IMPLEMENTATION MERGED / PHYSICAL ACCEPTANCE OPEN

Implementation merged through PR #67 at `99767b71a8e4ea20b6d587e4951c822a386019e5` and merged-main CI #342 is green.

Shipped content implementation:
- Smiling Sun — tracing-friendly early-child lesson;
- Friendly Owl — full Help Ladder animal lesson;
- Simple Rocket — Watch Then Draw;
- Easy Flower — grouped multi-stroke demonstrations.

Issue #60 remains open because the complete physical/content matrix was not rerun. Remaining P4.3 device/content coverage must be completed no later than P4.7; do not describe P4.3 as physically complete without that evidence.

### P4.4 — Free Draw Studio V1 Core — ACTIVE

Issue #61 / draft PR #69. QA candidate: `0.4.0-content-studio-p4.4-qa1`, versionCode 16.

Implemented scope:
- production Home → Free Draw route;
- blank/recovered stable working canvas;
- age-adaptive Pencil/Crayon/Marker/Eraser controls;
- child-safe palette and sizes;
- Undo/Redo;
- confirmation-only, undoable Clear;
- Save & leave / resume and lifecycle recovery;
- explicit Free Draw Gallery provenance;
- safe Gallery promotion/reopen/delete with working-document isolation;
- bounded scrollable controls to preserve canvas space on smaller phones;
- P4.4 automated regression coverage.

Exit gate: exact frozen QA1 head green in CI plus recorded physical/product QA from `docs/10-execution/P4_4_FREE_DRAW_QA.md`. Do not merge #69 on compile evidence alone.

### P4.5 — Coloring V1 Expansion — NEXT AFTER P4.4
Prepared-region fill where authored, guided coloring semantics, Color With Me progression, free coloring, persistence and line-art protection.

### P4.6 — Representative Content Set B + Cross-content QA
Add guided-region coloring, older-child detail/proportion and open-ended creative-variation lessons. Bring the representative Phase 4 set to at least seven production-quality lessons and validate age/journey coverage.

### P4.7 — End-to-end QA + 0.4.0 Release
Full offline/lifecycle/accessibility/device regression across catalog discovery, guided lessons, Free Draw, expanded coloring and Gallery. Complete any still-open P4.3 physical/content matrix, then deliver verified `0.4.0-content-studio` APK with reproducible release evidence.

## Post-Phase-4 direction

Sequencing remains phase-gated and can be refined after `0.4.0` evidence, but the intended path is:
- production companion expression/voice polish;
- expansion from the seven-lesson representative set toward the Public V1 24–36 lesson catalog;
- adaptive recommendations/help based on observable local learning signals, never punitive scoring;
- Parent Zone and parent-controlled export/settings;
- broader accessibility/device hardening and Beta;
- V1.0 only when product, privacy/safety, content and store-release gates are satisfied.

## Permanent delivery rule

Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
