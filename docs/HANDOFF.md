# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning application that behaves like a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development model: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

**Phase 0 — COMPLETE.**  
**Phase 1 — COMPLETE.**  
**Phase 2 — ACTIVE.** Target: `0.2.0-lesson-engine`.

## Frozen Phase 1 milestone

Version: `0.1.0-art-lab` / versionCode 11  
Release commit: `a448d664af8df2bb585326d726f0723461cd36c6`  
Tag: `v0.1.0-art-lab`  
Main CI run: #130 / `34676120442` — GREEN  
Profile artifact ID: `10292412312`  
Artifact ZIP digest: `sha256:0ae691d29ed5b0aa68d35fe97563cb692b374148ef50b9739d829580163a59c8`  
APK size: `14,718,726` bytes  
APK SHA-256: `7dbefd0053656abb8e1db989d69a0475dab6cca326f7aaec86f1da6909578e7e`

Release notes: `docs/releases/0.1.0-art-lab.md`.

### Proven milestone capabilities

- AndroidX Ink-backed low-latency finger drawing behind owned engine abstractions;
- 1000×1000 logical document coordinates;
- Pencil/Eraser/color/width;
- non-destructive erase masks;
- Undo/Redo/Clear/New;
- editable operation-based document truth;
- atomic Save/Reload/autosave + backup recovery + stale-save protection;
- deterministic teacher playback at 0.4× / 0.7× / 1× / 1.5× / 2×;
- Pause/Resume/Replay and captured-child-stroke teacher playback;
- teacher overlay isolated from child history/persistence;
- responsive engineering Art Lab controls and diagnostics;
- dedicated Quality Lab with stress/timing/memory/frame/soak harnesses;
- bounded raster/checkpoint committed renderer;
- offline operation with no Internet/ads/behavioral analytics/sensitive permissions.

### Physical quality evidence

Samsung SM-A546E / API 36 / ~7.4 GB RAM / 120 Hz:
- input P95/P99 2–3 ms — PASS;
- W2 Save×20 P95 360 ms worst recorded — PASS;
- W2 Load→editable P95 500 ms — PASS;
- W2 visible Undo/Redo P95 19 ms / P99 36 ms — PASS;
- W3 5,000-op document editable without crash/OOM — PASS;
- W1 committed frame gate: 603 frames, 0 native jank, 0.5% >16.7 ms — PASS;
- 30-minute soak: 4,587 cycles, 0 failures, final persistence verified, ~55.4 MiB Java / 58.5 MiB native end memory — PASS.

### Explicit pending-device coverage

Stylus-specific pressure/tilt/palm/inverted-eraser behavior and externally instrumented input-to-visible latency remain **PENDING-HARDWARE**. Do not silently convert these to PASS.

## Active task — Phase 2 / Lesson Engine 0.2

Epic: **#28 — Phase 2 Lesson Engine 0.2**.

Target: `0.2.0-lesson-engine`.

The Drawing Engine is now a frozen foundation. Phase 2 should consume it through owned interfaces rather than reopening its internals without a specific regression/ADR.

Planned order:
1. lesson package loader + schema validation;
2. lesson session state machine + restoration;
3. semantic step execution and teacher scheduling;
4. Draw With Me;
5. Watch Then Draw;
6. Trace & Learn;
7. pause/replay/speed/skip/help semantics;
8. lifecycle and failure recovery;
9. internal Lesson Lab + tests/CI/APK;
10. physical verification and `0.2.0-lesson-engine` packaging.

## Architecture constraints

- UI never owns artwork/history/lesson truth.
- AndroidX Ink stays behind drawing infrastructure adapters.
- teacher/trace overlays never become child artwork.
- persistence stores editable operations, not screenshots.
- core drawing/teaching remains offline-first.
- no mandatory child account, ads, behavioral analytics or sensitive permissions in engine milestones.
- required critical-path spend remains ₹0 where a professional free alternative exists.

## Key specs

- Drawing Engine: `docs/07_DRAWING_ENGINE_SPEC.md`
- Lesson Engine: `docs/08_LESSON_ENGINE_SPEC.md`
- Lesson/content schema: `docs/17_LESSON_CONTENT_SCHEMA.md` + `schemas/lesson.schema.json`
- Performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`
- Test strategy: `docs/11_TEST_STRATEGY.md`
- Release strategy: `docs/12_RELEASE_STRATEGY.md`
- Coloring Engine: `docs/19_COLORING_ENGINE_SPEC.md`
- Engine boundaries: `docs/20_ENGINE_BOUNDARIES.md`
- Visual system: `docs/21_VISUAL_SYSTEM.md`

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/releases/0.1.0-art-lab.md`
4. `ROADMAP.md`
5. Epic #28 and next incomplete dependency
6. relevant lesson/engine specifications

Never restart Drawing Engine architecture from scratch merely because the chat changed; `v0.1.0-art-lab` is the frozen baseline.
