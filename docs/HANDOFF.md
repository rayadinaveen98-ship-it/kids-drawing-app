# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

This file exists so a new conversation, developer, or agent can resume without reconstructing the project from chat history.

## What are we building?

An Android-first children's drawing and art-learning application. The product behaves like an exceptionally patient personal art teacher. Content, UI complexity, guidance, and tools adapt to age and demonstrated ability.

## Product promise

**Draw together with a teacher who never runs out of patience.**

## Development philosophy

Use a core-engine + vertical-slice approach:

Specify → Build capability → Unit/performance test → Integrate into real child flow → Test → lock milestone → expand.

Git is authoritative when chat and repository state disagree.

## Current project state

**Phase 0 — COMPLETE.** All foundational exit criteria are checked in `docs/14_PHASE0_EXIT_GATE.md`.

**Phase 1 — ACTIVE / ready for implementation.**

Target milestone: `0.1.0-art-lab`.

GitHub:
- Epic #7 — Phase 1 Art Lab / Drawing Engine 0.1
- #8 P1.1 scaffold Android project/modules/CI
- #9 P1.2 low-latency DrawingSurface + Ink adapter
- #10 P1.3 document/history
- #11 P1.4 persistence/recovery
- #12 P1.5 deterministic playback/5 paces
- #13 P1.6 Art Lab/debug metrics
- #14 P1.7 tests/benchmarks/stress
- #15 P1.8 APK/release packaging

**Next executable task: #8.**

## UX architecture

- Child shell: Home / Learn / Create / Gallery.
- Guided Drawing, Coloring and Free Draw use immersive creation workspaces with shell navigation hidden.
- Parent Zone is a separate gated graph.
- Safe Exit prevents system-back or close actions from silently destroying artwork.
- Incomplete sessions surface as Continue Drawing rather than being forced on startup.
- Canonical V1 screen IDs: `docs/15_SCREEN_ARCHITECTURE.md`.

## Content architecture

- Schema: `schemas/lesson.schema.json`.
- Validated reference lesson: `examples/cute-cat.lesson.json`.
- Authoring/package contract: `docs/16_LESSON_PACKAGE_AND_AUTHORING.md`.
- Taxonomy/curriculum: `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`.
- Public target: 36 guided lessons; hard release floor 24 high-quality complete lessons.

## Core engine contracts

- Drawing Engine: `docs/07_DRAWING_ENGINE_SPEC.md`.
- Performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`.
- Lesson Engine: `docs/08_LESSON_ENGINE_SPEC.md`.
- Coloring Engine: `docs/19_COLORING_ENGINE_SPEC.md`.
- Cross-engine ownership/handoffs: `docs/20_ENGINE_BOUNDARIES.md`.
- Stable AndroidX Ink 1.0.0 is the low-level inking substrate behind owned drawing-domain interfaces; see ADR-003 and ADR-007.

Critical engine rules:
- UI does not own lesson/coloring sequencing or artwork history.
- teacher/trace overlays never become child artwork.
- drawing→coloring handoff occurs only after durable document state exists.
- coloring uses the same editable artwork document, not a flattened screenshot.
- failures in narration, companion, coloring metadata or preview generation must not destroy child artwork.

## Companion / visual system

- Companion contract: `docs/09_COMPANION_SPEC.md`.
- Selected direction: **Premium Storybook Art Studio**.
- Visual system: `docs/21_VISUAL_SYSTEM.md`.
- Figma workspace: `Kids Drawing App — Phase 0.6 Visual System`, file key `2lGC11EPu2tjgrpYivJ8hf`.
- Figma Starter MCP quota currently blocks further automated frame generation; this does not block Art Lab engineering.

## Safety/privacy/release baseline

- Current review: `docs/22_SAFETY_PRIVACY_RELEASE_REVIEW.md`.
- Parent Gate: `docs/23_PARENT_GATE_SPEC.md`.
- Test strategy: `docs/11_TEST_STRATEGY.md`.
- Release strategy: `docs/12_RELEASE_STRATEGY.md`.

Defaults:
- no ads;
- no mandatory child account;
- no behavioral analytics for Alpha;
- no third-party crash SDK required for Phase 1;
- local/private artwork by default;
- no location, contacts, phone, camera, microphone, Bluetooth, broad storage or AD_ID permissions in V1 without new explicit review;
- every third-party runtime SDK requires child-directed/privacy/manifest review;
- first Play-bound build targets at least API 36 under the current 2026 baseline, rechecked at release time.

## Phase 1 outcome

`0.1.0-art-lab`: installable internal APK proving low-latency drawing, structured stroke capture, undo/redo, save/load and deterministic five-speed teacher playback, with tests and performance evidence.

Art Lab is an internal engineering surface, not the polished production children's UI.

## Never lose these constraints

- required software/service spend remains ₹0 on the critical path where a professional free option exists;
- core drawing/lesson use works offline;
- no cloud AI dependency in early critical path;
- no public child social features;
- no behavioral ads;
- important decisions belong in Git, not only chat.

## Resume protocol

At the start of any new chat/development session inspect:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. Epic #7 and its next incomplete dependency
6. relevant specification docs

When chat memory and repository state disagree, Git is authoritative.