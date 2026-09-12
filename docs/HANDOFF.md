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

Active issue: **#34 — P2.6 Lesson Lab, physical verification + `0.2.0-lesson-engine` release**.  
Starting baseline: **`main` after P2.5 merge `593c53f8a42e6749a0cf3e0a8b0a9f4c7f850e10` plus status-doc synchronization**.

## Frozen Phase 1 milestone

Version: `0.1.0-art-lab` / versionCode 11  
Release commit: `a448d664af8df2bb585326d726f0723461cd36c6`  
Tag: `v0.1.0-art-lab`  
Main CI run: #130 / `34676120442` — GREEN  
Profile artifact ID: `10292412312`  
APK SHA-256: `7dbefd0053656abb8e1db989d69a0475dab6cca326f7aaec86f1da6909578e7e`

Release notes: `docs/releases/0.1.0-art-lab.md`.

### Proven Phase 1 capabilities

- AndroidX Ink-backed low-latency finger drawing behind owned abstractions;
- editable operation-based document truth with Pencil/Eraser/color/width, Undo/Redo/Clear/New;
- atomic Save/Reload/autosave + backup recovery + stale-save protection;
- deterministic teacher playback at five frozen pace profiles;
- teacher overlay isolated from child history/persistence;
- Art Lab + Quality Lab stress/timing/memory/frame/soak harnesses;
- offline operation with no Internet/ads/behavioral analytics/sensitive permissions.

### Physical quality evidence

Samsung SM-A546E / API 36 / ~7.4 GB RAM / 120 Hz:
- input P95/P99 2–3 ms — PASS;
- Save×20 P95 360 ms — PASS;
- Load→editable P95 500 ms — PASS;
- visible Undo/Redo P95 19 ms / P99 36 ms — PASS;
- 5,000-op document — PASS;
- committed frame gate: 603 frames, 0 native jank, 0.5% >16.7 ms — PASS;
- 30-minute soak: 4,587 cycles, 0 failures — PASS.

Stylus pressure/tilt/palm/inverted-eraser and externally instrumented input-to-visible latency remain **PENDING-HARDWARE**.

## Phase 2 progress

Epic: **#28 — Phase 2 Lesson Engine 0.2**.  
Target: `0.2.0-lesson-engine`.

### P2.1 — COMPLETE

Issue #29 / PR #35. Established runtime lesson models, strict package parsing/validation, typed diagnostics, asset adapter and bundled `lessons/cute-cat` authored reference package. Android CI #137 / `34677550750` GREEN.

### P2.2 — COMPLETE

Issue #30 / PR #36. Established pure-Kotlin deterministic lesson session state, commands/rejections, mode/pace/cursor/help/overview context, pause/resume, semantic snapshots, transient normalization and exact lesson revision/step compatibility.

### P2.3 — COMPLETE

Issue #31 / PR #37. Established real teacher-step execution and Draw With Me over the frozen Drawing Engine boundary, including deterministic teacher playback requests and child-turn progression without teacher strokes entering child artwork.

### P2.4 — COMPLETE

Issue #32 / PR #38. Squash merge `16828eee0988b8143559857a892c7d927dfc1c0d`. Exact-head Android CI #146 GREEN.

Established:
- Watch Then Draw full-lesson overview + child drawing pass;
- pause/resume/pace/skip overview semantics;
- replay-on-demand;
- Trace & Learn teacher execution + authored trace guides;
- Help Ladder progression, missing-level skipping, Reduce/Dismiss Help;
- guide clear/restore semantics;
- mode-specific Cute Cat completion tests;
- proof teacher/guide strokes remain outside child history.

### P2.5 — COMPLETE

Issue #33 / PR #39. Squash merge `593c53f8a42e6749a0cf3e0a8b0a9f4c7f850e10`. Exact PR head `aad7f1cffc1b3aa8ad465870d0f41f48f59ae201`. Android CI #180 / `34682787683` GREEN.

Established:
- versioned SHA-256 lesson-session snapshot envelope;
- atomic session target/backup/temp store and per-session stale-save protection;
- typed Missing / Loaded / Corrupt recovery;
- lifecycle autosave on stable lesson/help/drawing/failure/post-drawing boundaries;
- child-document-first recovery ordering;
- fatal/incompatible lesson outcomes preserve any already recovered child artwork;
- fresh runtime generation for restored teacher/overview request IDs;
- stale pre-recreation callbacks reject deterministically;
- trace/help guide rehydration without child-history contamination;
- paused teacher restart deferred until Resume;
- persisted teacher-restart intent across failure/process death/Save & Exit;
- RetryRecoverable behavior for teacher/overview playback failures;
- post-drawing `ChooseColorWithMe`, `ChooseColorMyself`, `FinishForNow` contracts;
- coloring handoff failure returns safely to retryable post-drawing choice;
- process recreation during handoff normalizes safely;
- all-three-mode recreation, corruption/fault injection, stale callback, fatal-revision and post-drawing persistence tests.

### P2.6 — ACTIVE / NEXT

Issue **#34 — Lesson Lab + physical verification + `0.2.0-lesson-engine` milestone release**.

Definition of done must include:
- integrated Lesson Lab exercising the real Lesson Engine, Drawing Engine and authored Cute Cat package rather than mocks;
- all three teaching modes and five pace profiles available to engineering verification;
- lifecycle/recovery and overlay-isolation diagnostics exposed sufficiently for verification;
- complete automated CI gates green;
- installable milestone APK produced;
- physical Android verification recorded for required P2.6 scenarios;
- milestone version/tag/release evidence and implementation/QA report committed;
- any hardware-only exclusions remain explicit, never silently marked PASS.

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
