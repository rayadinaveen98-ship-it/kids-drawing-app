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

Active issue: **#30 — P2.2 deterministic lesson session state machine + snapshots**.  
Active branch: **`phase2/session-state-machine`**.

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

## Phase 2 progress

Epic: **#28 — Phase 2 Lesson Engine 0.2**.

Target: `0.2.0-lesson-engine`.

### P2.1 — COMPLETE

Issue: **#29 — Lesson package loader + runtime validation**  
PR: **#35**  
Merged `main` commit: `74d0de35a705d41747f7bb47e0b4383905c0f806`  
Exact hardened branch head: `dd9f0f86ebcb57afac09c0c079dfe62dd9c96634`  
Android CI run #137 / `34677550750`: **GREEN**.

P2.1 established:
- Kotlin serialization wiring;
- product-owned runtime lesson models matching `schemas/lesson.schema.json`;
- strict pure-Kotlin package parsing/validation;
- typed diagnostics;
- Android asset adapter only at the infrastructure boundary;
- permanent bundled `lessons/cute-cat` reference package;
- real authored teacher strokes and trace/help guides;
- restored `docs/17_LESSON_CONTENT_SCHEMA.md`;
- negative tests for malformed JSON, content API/version constraints, unsafe paths, duplicate IDs, missing refs, trace support and authored stroke validity.

### P2.2 — ACTIVE

Issue: **#30 — Deterministic lesson session state machine + snapshots**.

Current implementation direction:
- pure Kotlin/session-domain code only;
- UI dispatches commands but cannot set state;
- reuses frozen Drawing Engine `TeachingPace`;
- typed `Ready`, overview, drawing sub-phases, `Paused(previousStableState)`, post-drawing and terminal states;
- deterministic accepted/rejected command results;
- Start/Pause/Resume/SetPace/SaveAndExit foundation;
- immutable active context carries mode, pace, step index/stable ID, help level and overview status;
- snapshots store lesson revision/session/document identity plus semantic progress;
- unsafe transient phases normalize before persistence/restore;
- exact lesson revision and step identity are checked before restoration;
- tests use the real bundled Cute Cat package and cover all three modes/all five paces/illegal commands/pause-resume/snapshot compatibility.

Do not mark P2.2 complete until exact-head CI is green and the PR is merged.

## Remaining Phase 2 order

1. #30 — session state machine + snapshots — **ACTIVE**
2. #31 — teacher step execution + Draw With Me
3. #32 — Watch Then Draw + Trace & Learn + Help Ladder
4. #33 — lifecycle/session persistence + failure recovery
5. #34 — Lesson Lab + physical verification + `0.2.0-lesson-engine` release

## Architecture constraints

- UI never owns artwork/history/lesson truth.
- UI cannot set arbitrary session states.
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
