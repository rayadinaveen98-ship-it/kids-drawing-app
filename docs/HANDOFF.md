# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning application that behaves like a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development model: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

**Phase 0 — COMPLETE.**  
**Phase 1 — ACTIVE.** Target: `0.1.0-art-lab`.

Progression:
- #8 P1.1 scaffold/CI — **COMPLETE**
- #9 P1.2 DrawingSurface/Ink adapter — **COMPLETE**
- #10 P1.3 document/history — **COMPLETE**
- #11 P1.4 persistence/recovery — **COMPLETE**
- #12 P1.5 deterministic teacher playback/five paces — **COMPLETE**
- #13 P1.6 Art Lab controls/debug metrics — **COMPLETE**
- #14 P1.7 tests/benchmarks/stress — **ACTIVE**
- #15 P1.8 `0.1.0-art-lab` packaging/verification — **PENDING**

## Last stable milestone — P1.6

PR **#21** squash-merged as `3f09716ed8db11520a6f045010a361a1819105d7`.

Physically verified build:
- `0.0.5-p1.6-test` / versionCode 5;
- exact branch head `ada65e4bdc30e2689c51e819e208d85e2b4ec2da`;
- CI run #64 / `34636448915` — green;
- artifact ID `10278896739`;
- APK size `18,226,354` bytes;
- APK SHA-256 `3ff4b1fbb3e00459d0430a6f02341b5e3170626b5d8d38647772feec200b7db3`;
- user physically verified all requested P1.6 behavior as working.

Capabilities now proven on-device include low-latency drawing, product-owned Pencil/Eraser/color/width state, non-destructive erase masks with Undo/Redo, Clear + Undo, New/Save/Reload/reopen, captured teacher strokes, five playback speeds, Pause/Resume/Replay, responsive phone layout and live debug diagnostics.

## Active task — P1.7

Issue **#14** turns the Drawing Engine contracts into repeatable quality evidence.

Required work:
- generated 2,000 and 5,000 operation documents;
- long-history undo/redo stress;
- save/load/recovery/corruption stress;
- deterministic playback stress at all five speeds;
- instrumentation/performance measurement where useful;
- frame/input/memory evidence against `docs/18_DRAWING_PERFORMANCE_GATES.md`;
- 30-minute soak procedure/template;
- low-end/mainstream/stylus device class results recorded only as `PASS`, `FAIL`, or `PENDING-HARDWARE`.

Do not silently pass hardware-only gates from CI.

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

## Next executable sequence

1. Create `phase1/p1.7-quality-stress` from current `main`.
2. Add generated stress fixtures and automated high-volume domain/persistence/playback tests.
3. Add benchmark/instrumentation support that can produce comparable evidence on physical hardware.
4. Add soak and device-result templates.
5. Run CI and resolve every P0/P1 failure.
6. Record hardware results honestly.
7. Close #14 only when acceptance evidence is complete or explicitly `PENDING-HARDWARE` where allowed.
8. Start #15 and package/tag the formal `0.1.0-art-lab` APK.

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. Epic #7 and next incomplete dependency
6. relevant specs/issues
