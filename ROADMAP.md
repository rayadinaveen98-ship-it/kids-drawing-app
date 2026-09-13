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

### P4.3 — Representative Content Set A — ACTIVE
Ship four new production lesson packages proving:
- tracing-friendly early-child content;
- full Help Ladder animal teaching;
- Watch Then Draw;
- grouped multi-stroke demonstrations.

Chosen set: Smiling Sun, Friendly Owl, Simple Rocket and Easy Flower. Cute Cat r1 remains the frozen regression lesson.

### P4.4 — Free Draw Studio V1 Core — NEXT
Blank canvas; age-adaptive Pencil/Crayon/Marker/Eraser; palette/size; Undo/Redo; safe Clear; persistence/recovery; Gallery promotion.

### P4.5 — Coloring V1 Expansion
Prepared-region fill where authored, guided coloring semantics, Color With Me progression, free coloring, persistence and line-art protection.

### P4.6 — Representative Content Set B + Cross-content QA
Add guided-region coloring, older-child detail/proportion and open-ended creative-variation lessons. Bring the representative Phase 4 set to at least seven production-quality lessons and validate age/journey coverage.

### P4.7 — End-to-end QA + 0.4.0 Release
Full offline/lifecycle/accessibility/device regression across catalog discovery, guided lessons, Free Draw, expanded coloring and Gallery. Deliver verified `0.4.0-content-studio` APK with reproducible release evidence.

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
