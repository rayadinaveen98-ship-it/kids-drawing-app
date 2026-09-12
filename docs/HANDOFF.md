# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning application that behaves like a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development model: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

**Phase 0 — COMPLETE.**  
**Phase 1 — FINAL PACKAGING.** Target: `0.1.0-art-lab`.

Progression:
- #8 P1.1 scaffold/CI — **COMPLETE**
- #9 P1.2 DrawingSurface/Ink adapter — **COMPLETE**
- #10 P1.3 document/history — **COMPLETE**
- #11 P1.4 persistence/recovery — **COMPLETE**
- #12 P1.5 deterministic teacher playback/five paces — **COMPLETE**
- #13 P1.6 Art Lab controls/debug metrics — **COMPLETE**
- #14 P1.7 tests/benchmarks/stress — **COMPLETE**
- #15 P1.8 `0.1.0-art-lab` packaging/verification — **ACTIVE**

## Last stable engineering gate — P1.7

PR **#22** squash-merged to `main` as `e7cefac6a0705838f209bf591dd253618325719b`.

### Proven physical behavior

Samsung SM-A546E / API 36 / ~7.4 GB RAM / 120 Hz:
- input dispatch upper bound P95/P99 2–3 ms — PASS;
- W2 save P95 360 ms worst recorded — PASS;
- W2 load→editable P95 500 ms — PASS;
- W2 visible Undo/Redo P95 19 ms / P99 36 ms — PASS;
- W3 5,000-op document editable without crash/OOM — PASS;
- W1 committed raster frame gate: 603 frames, 0 native jank, 0.5% >16.7 ms — PASS;
- 30-minute soak: 4,587 cycles, 0 failures, final persistence verified, ~55.4 MiB Java / 58.5 MiB native end memory — PASS.

### Explicit pending-device coverage

Stylus-specific pressure/tilt/palm/inverted-eraser behavior and externally instrumented input-to-visible latency remain **PENDING-HARDWARE**. Do not silently convert these to PASS.

## Active task — P1.8

Issue **#15** packages the completed Drawing Engine 0.1 work into the first formal milestone.

Release branch: `phase1/p1.8-art-lab-release`  
Version: `0.1.0-art-lab`  
versionCode: `11`

Required before P1.8 close:
1. final release notes + changelog/status synchronization;
2. exact release commit passes CI;
3. release-like profile APK artifact recorded with size/SHA-256;
4. release PR merged;
5. known-good milestone commit tagged;
6. #15 and Epic #7 closed with full evidence.

## Proven milestone capabilities

- AndroidX Ink-backed low-latency finger drawing behind owned engine abstractions;
- 1000×1000 logical document coordinates;
- product-owned Pencil/Eraser/color/width state;
- non-destructive erase masks;
- Undo/Redo/Clear/New;
- editable operation-based document truth;
- atomic save/reload/autosave + backup recovery;
- deterministic teacher playback at 0.4× / 0.7× / 1× / 1.5× / 2×;
- Pause/Resume/Replay and captured-child-stroke teacher playback;
- teacher overlay isolated from child history/persistence;
- responsive engineering Art Lab controls;
- dedicated Quality Lab with stress, timing, memory, frame, and soak harnesses;
- raster/checkpoint committed rendering for bounded normal-frame/history cost;
- offline operation with no Internet/ads/behavioral analytics/sensitive permissions.

## Architecture constraints

- UI never owns artwork/history/lesson truth.
- AndroidX Ink stays behind drawing infrastructure adapters.
- teacher/trace overlays never become child artwork.
- persistence stores editable operations, not screenshots.
- core drawing/playback works offline.
- no mandatory child account, ads, behavioral analytics or sensitive permissions in Art Lab.
- required critical-path spend remains ₹0 where a professional free alternative exists.

## Key specs

- Drawing Engine: `docs/07_DRAWING_ENGINE_SPEC.md`
- Performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`
- Test strategy: `docs/11_TEST_STRATEGY.md`
- Release strategy: `docs/12_RELEASE_STRATEGY.md`
- Lesson Engine: `docs/08_LESSON_ENGINE_SPEC.md`
- Coloring Engine: `docs/19_COLORING_ENGINE_SPEC.md`
- Engine boundaries: `docs/20_ENGINE_BOUNDARIES.md`
- Visual system: `docs/21_VISUAL_SYSTEM.md`

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. Epic #7 and next incomplete dependency
6. relevant specs/issues

If the release branch is still active, finish #15 before starting Phase 2.
