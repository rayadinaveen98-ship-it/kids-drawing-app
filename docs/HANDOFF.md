# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

This file lets a new conversation, developer or agent resume without reconstructing the project from chat history. Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning application that behaves like an exceptionally patient personal art teacher. Content, UI complexity, guidance and tools adapt to age and demonstrated ability.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development philosophy: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

**Phase 0 — COMPLETE.** See `docs/14_PHASE0_EXIT_GATE.md`.

**Phase 1 — ACTIVE.** Target milestone: `0.1.0-art-lab`.

Progression:
- #8 P1.1 scaffold/CI — **COMPLETE**
- #9 P1.2 DrawingSurface/Ink adapter — **COMPLETE**
- #10 P1.3 document/history — **COMPLETE**
- #11 P1.4 persistence/recovery — **COMPLETE**
- #12 P1.5 deterministic teacher playback/five paces — **COMPLETE**
- #13 P1.6 Art Lab controls/debug metrics — **ACTIVE**
- #14 P1.7 tests/benchmarks/stress — **NEXT**
- #15 P1.8 `0.1.0-art-lab` packaging/verification — **PENDING**

Current working branch: `phase1/p1.6-art-lab-controls`  
Current draft PR: **#21**

## Last stable milestone

P1.5 merged through PR **#20** as `7767fdcbf8454358954c6cbc0187f0933b97099e`.

Physically verified build:
- version `0.0.4-p1.5-test` / versionCode 4;
- CI run #53 / `34634192081` — green;
- exact branch head `394454d8082db5c65188df6efddc6bbc37b12eec`;
- artifact ID `10276798814`;
- ZIP digest `sha256:59f830dea25cb3d0e7ecf2a1abda983b82129f93a113101a4337001d2af6a4df`;
- APK size `18,193,581` bytes;
- APK SHA-256 `03b0fcba4c3610898605ecdad2f1635f6090a2eca7cdd14383ccf9ce24a4708c`;
- user reported the build working fine on real Android hardware.

P1.5 proves canonical teacher strokes can render as a separate non-destructive overlay at 0.40× / 0.70× / 1.00× / 1.50× / 2.00× with pause/resume/replay and mid-stroke pace changes while child `doc ops` remain independent.

## P1.6 objective

Issue **#13**: make every Drawing Engine 0.1 capability directly exercisable from one internal Art Lab screen.

Required controls:
- New/blank document;
- Pencil;
- Eraser;
- color;
- width;
- Undo / Redo / Clear;
- Save / Reload;
- load/import/record a teacher test source;
- five pace controls;
- Play / Pause / Resume / Replay;
- debug metrics toggle.

Debug panel should expose tool/color/width, document operation counts, history cursor/depth, viewport transform, input tool/pressure, teacher status/pace/virtual time, persistence status, and useful timing metrics.

Current implementation direction on PR #21:
- product-owned `DrawingToolEngine` is authoritative for Pencil/Eraser/color/width;
- erasing commits `EraseMaskRecord` operations rather than destructive pixels or white persisted ink;
- committed renderer projects ink + erase operations in order;
- `DrawingEngineState` exposes history cursor/depth/redo depth;
- surface metrics expose viewport and selected tool diagnostics;
- `TeacherPlaybackSession` owns loaded teacher source and playback state;
- Art Lab can load the canonical demo or copy the last child stroke into an isolated teacher source.

## Architecture constraints

- UI never owns artwork/history/lesson truth.
- AndroidX Ink stays behind drawing infrastructure adapters.
- teacher/trace overlays never become child artwork.
- persistence stores editable operations, not screenshots.
- Drawing → Coloring later requires durable artwork before handoff.
- companion/narration/preview failures must never destroy artwork.
- core drawing/playback works offline.
- no mandatory child account, ads, behavioral analytics or sensitive permissions in Art Lab.

## Important specs

- Drawing Engine: `docs/07_DRAWING_ENGINE_SPEC.md`
- Performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`
- Lesson Engine: `docs/08_LESSON_ENGINE_SPEC.md`
- Coloring Engine: `docs/19_COLORING_ENGINE_SPEC.md`
- Cross-engine boundaries: `docs/20_ENGINE_BOUNDARIES.md`
- Visual system: `docs/21_VISUAL_SYSTEM.md`
- Safety/release review: `docs/22_SAFETY_PRIVACY_RELEASE_REVIEW.md`
- Parent Gate: `docs/23_PARENT_GATE_SPEC.md`

## Content / UX reference

- Lesson schema: `schemas/lesson.schema.json`
- Reference lesson: `examples/cute-cat.lesson.json`
- Screen architecture: `docs/15_SCREEN_ARCHITECTURE.md`
- Starter curriculum: `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`
- Public V1 content target: 30–40 guided lessons; hard floor 24 high-quality complete lessons.
- Visual direction: **Premium Storybook Art Studio**.
- Figma file key: `2lGC11EPu2tjgrpYivJ8hf`.

## Next executable task

Continue **#13 / P1.6** on PR #21 until CI is green, then package a dedicated P1.6 test APK and physically verify:
1. Pencil color/width changes;
2. true Eraser + Undo/Redo;
3. Clear + Undo;
4. New document + persistence;
5. teacher demo and captured-last-stroke playback;
6. five paces + Pause/Resume/Replay;
7. debug overlay values;
8. phone-width usability.

After physical PASS, merge #21, close #13, update this handoff, and begin **#14 / P1.7 tests/benchmarks/recovery stress**.

## Never lose these constraints

- required critical-path spend remains ₹0 where a professional free alternative exists;
- no cloud AI dependency in the early critical path;
- no public child social features;
- no behavioral ads;
- meaningful Android milestones should produce an installable APK;
- important decisions/evidence belong in Git, not only chat.

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. Epic #7 and next incomplete dependency
6. relevant specs/issues
