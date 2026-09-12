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
**Phase 2 implementation + physical QA — COMPLETE.**  
**Next product milestone:** Phase 3 / `0.3.0-vertical-slice` after release administration is finalized.

Latest verified Android milestone: **`0.2.0-lesson-engine` / versionCode 12**.

Verified APK evidence:
- app-code commit `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`;
- merged-main Android CI #204 / `34685747984` GREEN;
- profile artifact ID `10294878922`;
- size `14,984,842` bytes;
- SHA-256 `656bee202bc71d5b487f287c6dc3a8b3f05f4cc50eea58c7477fd0a59534e109`.

Target tag: `v0.2.0-lesson-engine`. Do not claim the tag exists unless verified in GitHub; the current connected execution tool cannot create tags/releases.

## Frozen Phase 1 milestone

Version `0.1.0-art-lab` / versionCode 11, tag `v0.1.0-art-lab`, release commit `a448d664af8df2bb585326d726f0723461cd36c6`.

Proven Phase 1 capabilities remain frozen:
- AndroidX Ink-backed low-latency finger drawing behind owned abstractions;
- operation-based editable document/history with Pencil/Eraser/color/width, Undo/Redo/Clear/New;
- atomic save/reload/autosave + backup recovery + stale-save protection;
- deterministic teacher playback at five pace profiles;
- teacher overlay isolated from child history/persistence;
- Art Lab + Quality Lab stress/timing/memory/frame/soak harnesses;
- offline operation with no Internet/ads/behavioral analytics/sensitive permissions.

Stylus pressure/tilt/palm/inverted-eraser and externally instrumented input-to-visible latency remain **PENDING-HARDWARE**.

## Phase 2 — Lesson Engine 0.2

Epic: **#28**.

### P2.1 — COMPLETE
Issue #29 / PR #35. Runtime lesson models, strict package parsing/validation, typed diagnostics, asset adapter and bundled `lessons/cute-cat` authored package.

### P2.2 — COMPLETE
Issue #30 / PR #36. Deterministic lesson-session state, commands/rejections, mode/pace/cursor/help/overview context, pause/resume and semantic snapshots.

### P2.3 — COMPLETE
Issue #31 / PR #37. Real teacher-step execution and Draw With Me over the frozen Drawing Engine boundary.

### P2.4 — COMPLETE
Issue #32 / PR #38. Watch Then Draw, Trace & Learn, authored trace guides, replay and Help Ladder. Squash merge `16828eee0988b8143559857a892c7d927dfc1c0d`.

### P2.5 — COMPLETE
Issue #33 / PR #39. Lifecycle/session persistence, child-document-first recovery, fresh runtime generations, stale callback rejection, recoverable playback retry and post-drawing handoff recovery. Squash merge `593c53f8a42e6749a0cf3e0a8b0a9f4c7f850e10`; exact-head Android CI #180 / `34682787683` GREEN.

### P2.6 — IMPLEMENTATION + PHYSICAL QA COMPLETE
Issue #34. Real Lesson Lab + bundled Cute Cat + all modes/paces + recovery/isolation/failure/handoff diagnostics.

- PR #40 exact-head CI #201 GREEN; squash merge `abcf4cf8fcd846975e8aa5bb16e3e03ffaa116a2`.
- Portrait responsive hotfix PR #41 exact-head CI #203 GREEN; squash merge `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`.
- merged-main Android CI #204 / `34685747984` GREEN.
- physical QA on Samsung SM-A546E/API 36: **32/32 PASS**.
- all three modes and all five paces passed physically.
- force-stop/relaunch recovery passed for teacher playback, child turn, HelpActive and post-drawing choice.
- fresh teacher request generation physically demonstrated `g0 → g1`.
- persisted diagnostics remained `teacher/guide in child history: 0 · PASS`.
- Art Lab and Quality Lab regression checks passed.
- no crash/deadlock observed across the full physical matrix.

Full sheet: `docs/10-execution/P2_6_PHYSICAL_QA.md`.
Release evidence: `docs/releases/0.2.0-lesson-engine.md`.

## Architecture constraints

- UI never owns artwork/history/lesson truth.
- UI cannot set arbitrary session states.
- AndroidX Ink stays behind drawing infrastructure adapters.
- teacher/trace/help overlays never become child artwork.
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
3. `docs/releases/0.2.0-lesson-engine.md`
4. `ROADMAP.md`
5. current Phase 3 issue/brief once created
6. relevant Lesson/Coloring/Visual specs

Do not restart Drawing Engine or Lesson Engine architecture merely because the chat changed. Phase 1 and the verified Phase 2 engine foundation are frozen baselines.